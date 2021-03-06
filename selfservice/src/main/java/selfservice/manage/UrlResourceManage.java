package selfservice.manage;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class UrlResourceManage extends ClassPathResourceManage implements HealthIndicator {

    private final static ZoneId GMT = ZoneId.of("GMT");

    private final String manageBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final int refreshInMinutes;
    private final HttpHeaders httpHeaders;
    private String body = "{\"ALL_ATTRIBUTES\":true}";

    private ScheduledFuture<?> scheduledFuture;
    private boolean lastCallFailed = true;

    private volatile ZonedDateTime lastRefreshCheck = ZonedDateTime.now(GMT);
    private volatile ZonedDateTime metadataLastUpdated = ZonedDateTime.now(GMT);

    public UrlResourceManage(
        String username,
        String password,
        String manageBaseUrl,
        int refreshInMinutes,
        Resource singleTenantsConfigPath) {
        super(false, singleTenantsConfigPath);
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        this.manageBaseUrl = manageBaseUrl;
        this.refreshInMinutes = refreshInMinutes;

        this.httpHeaders = new HttpHeaders();
        this.httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        this.httpHeaders.add(HttpHeaders.AUTHORIZATION, basicAuth);

        SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate
            .getRequestFactory();
        requestFactory.setConnectTimeout(5 * 1000);

        schedule(this.refreshInMinutes, TimeUnit.MINUTES);
        doInitializeMetadata();
    }

    private void schedule(int period, TimeUnit timeUnit) {
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }
        this.scheduledFuture = newScheduledThreadPool(1).scheduleAtFixedRate(this::initializeMetadata, period,
            period, timeUnit);
    }


    @Override
    protected Resource getIdpResource() throws UnsupportedEncodingException {
        LOG.debug("Fetching IDP metadata entries from {}", manageBaseUrl);
        ResponseEntity<String> responseEntity = restTemplate.exchange
            (manageBaseUrl + "/manage/api/internal/search/saml20_idp", HttpMethod.POST,
                new HttpEntity<>(this.body, this.httpHeaders), String.class);
        return new ByteArrayResource(responseEntity.getBody().getBytes("UTF-8"));
    }

    @Override
    protected Resource getSpResource() throws UnsupportedEncodingException {
        LOG.debug("Fetching SP metadata entries from {}", manageBaseUrl);
        ResponseEntity<String> responseEntity = restTemplate.exchange
            (manageBaseUrl + "/manage/api/internal/search/saml20_sp", HttpMethod.POST,
                new HttpEntity<>(this.body, this.httpHeaders), String.class);
        return new ByteArrayResource(responseEntity.getBody().getBytes("UTF-8"));
    }

    private void doInitializeMetadata() {
        try {
            this.lastRefreshCheck = ZonedDateTime.now(GMT);
            this.metadataLastUpdated = ZonedDateTime.now(GMT);
            super.initializeMetadata();
            //now maybe this is the first successful call after a failure, so check and change the refreshInMinutes
            if (lastCallFailed) {
                schedule(refreshInMinutes, TimeUnit.MINUTES);
            }
            lastCallFailed = false;
        } catch (Throwable e) {
            /*
             * By design we catch the error and not rethrow it.
             *
             * UrlResourceManage has timing issues when the server reboots and required MetadataExporter
             * endpoints
             * are not available yet. We re-schedule the timer to try every 15 seconds until it's succeeds
             */
            LOG.error("Error in refreshing / initializing metadata", e);
            lastCallFailed = true;
            schedule(15, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void initializeMetadata() {
        doInitializeMetadata();
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = lastRefreshCheck.plusMinutes(refreshInMinutes + 2).isBefore(ZonedDateTime.now
            (GMT)) ? Health.down() : Health.up();

        return healthBuilder
            .withDetail("lastMetaDataRefreshCheck", lastRefreshCheck.format(DateTimeFormatter.ISO_DATE_TIME))
            .withDetail("serviceProvidersLastUpdated", metadataLastUpdated.format(DateTimeFormatter.ISO_DATE_TIME))
            .build();
    }

}
