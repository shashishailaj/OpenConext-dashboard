package selfservice.dao;

import java.util.List;

import selfservice.domain.csa.InUseFacetValue;

public interface FacetValueDaoCustom {

  void linkCspToFacetValue(long compoundProviderServiceId, long facetValueId);

  void unlinkCspFromFacetValue(long compoundProviderServiceId, long facetValueId);

  void unlinkAllCspFromFacetValue(long facetValueId);

  void unlinkAllCspFromFacet(long facetId);

  List<InUseFacetValue> findInUseFacetValues(long facetValueId);

  List<InUseFacetValue> findInUseFacet(long facetId);

}
