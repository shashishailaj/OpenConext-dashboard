/** @jsx React.DOM */

App.Components.HowToConnectPanel = React.createClass({
  mixins: [
    React.addons.LinkedStateMixin
  ],

  getInitialState: function() {
    return {
      currentStep: this.props.app.connected ? "disconnect" : "info",
      accepted: false,
      comments: ""
    }
  },

  render: function() {
    switch (this.state.currentStep) {
      case "disconnect":
        return this.renderDisconnectStep();
      case "connect":
        return this.renderConnectStep();
      case "done":
        return this.renderDoneStep();
      case "done-disconnect":
        return this.renderDoneDisconnectStep();
      default:
        return this.renderInfoStep();
    }
  },

  renderInfoStep: function() {
    return (
      <div className="l-middle">
        <div className="mod-title">
          <h1>{I18n.t("how_to_connect_panel.info_title")}</h1>
          <p>{I18n.t("how_to_connect_panel.info_sub_title")}</p>
        </div>

        <div className="mod-connect">
          <div className="box">
            <div className="content">
              <h2>{I18n.t("how_to_connect_panel.checklist")}</h2>
              <ul>
                <li>
                  {I18n.t("how_to_connect_panel.check")}&nbsp;
                  <a onClick={this.props.onSwitchPanel("license_info")} href="#">
                    {I18n.t("how_to_connect_panel.license_info")}
                  </a>
                </li>

                <li>
                  {I18n.t("how_to_connect_panel.check")}&nbsp;
                  <a onClick={this.props.onSwitchPanel("attribute_policy")} href="#">
                    {I18n.t("how_to_connect_panel.attributes_policy")}
                  </a>
                </li>

                <li>
                  {I18n.t("how_to_connect_panel.read")}&nbsp;
                  <a href={I18n.t("how_to_connect_panel.wiki_link")} target="_blank">
                    {I18n.t("how_to_connect_panel.wiki")}
                  </a>
                </li>
              </ul>
              <h2>{I18n.t("how_to_connect_panel.specific.title")}</h2>
              <p>{I18n.t("how_to_connect_panel.specific.description")}</p>
              <ul>
                <li>
                  {I18n.t("how_to_connect_panel.read")}&nbsp;
                  <a href={I18n.t("how_to_connect_panel.wiki_link")} target="_blank">
                    {I18n.t("how_to_connect_panel.wiki")}
                  </a>
                </li>
              </ul>
            </div>
          </div>
          <p className="cta">
            <a href="#" className="c-button" onClick={this.handleGotoStep("connect")}>{I18n.t("how_to_connect_panel.connect")}</a>
            <em>{I18n.t("how_to_connect_panel.connect_hint")}</em>
          </p>
        </div>
      </div>
    );
  },

  renderConnectStep: function() {
    return (
      <div className="l-middle">
        <div className="mod-title">
          <h1>{I18n.t("how_to_connect_panel.connect_title", {app: this.props.app.name})}</h1>
        </div>

        <div className="mod-connect">
          <div className="box">
            <div className="content">
              <div className="number">1</div>
              <h2>{I18n.t("how_to_connect_panel.terms_title")}</h2>
              <ul>
                <li>
                  {I18n.t("how_to_connect_panel.provide_attributes.before")}
                  <a onClick={this.props.onSwitchPanel("attribute_policy")} href="#">
                    {I18n.t("how_to_connect_panel.attributes")}
                  </a>
                  {I18n.t("how_to_connect_panel.provide_attributes.after")}
                </li>

                <li>
                  {I18n.t("how_to_connect_panel.forward_permission.before")}
                  <a onClick={this.props.onSwitchPanel("attribute_policy")} href="#">
                    {I18n.t("how_to_connect_panel.attributes")}
                  </a>
                  {I18n.t("how_to_connect_panel.forward_permission.after", { app: this.props.app.name })}
                </li>

                <li>
                  {I18n.t("how_to_connect_panel.obtain_license.before")}
                  <a onClick={this.props.onSwitchPanel("license_info")} href="#">
                    {I18n.t("how_to_connect_panel.license")}
                  </a>
                  {I18n.t("how_to_connect_panel.obtain_license.after", { app: this.props.app.name })}
                </li>
              </ul>
              <label>
                <input type="checkbox" checkedLink={this.linkState("accepted")} />

                {I18n.t("how_to_connect_panel.accept")}
              </label>
            </div>
            <hr />
            <div className="content">
              <div className="number">2</div>
              <h2>{I18n.t("how_to_connect_panel.comments_title")}</h2>
              <p>{I18n.t("how_to_connect_panel.comments_description")}</p>
              <textarea valueLink={this.linkState("comments")} placeholder={I18n.t("how_to_connect_panel.comments_placeholder")} />
            </div>
          </div>
          <p className="cta">
            <a href="#" className={"c-button " + (this.state.accepted ? "" : "disabled")} onClick={this.handleMakeConnection}>{I18n.t("how_to_connect_panel.connect")}</a>
            <a href="#" className="c-button cancel" onClick={this.handleGotoStep("info")}>{I18n.t("how_to_connect_panel.cancel")}</a>
          </p>
        </div>
      </div>
    );
  },

  renderDoneStep: function() {
    return (
      <div className="l-middle">
        <div className="mod-title">
          <h1>{I18n.t("how_to_connect_panel.done_title")}</h1>
          <p dangerouslySetInnerHTML={{ __html: I18n.t("how_to_connect_panel.done_subtitle_html") }} />
          <br />
          <p className="cta">
            <a href="/apps" className="c-button">{I18n.t("how_to_connect_panel.back_to_apps")}</a>
          </p>
        </div>
      </div>
    );
  },

  renderDoneDisconnectStep: function() {
    return (
      <div className="l-middle">
        <div className="mod-title">
          <h1>{I18n.t("how_to_connect_panel.done_disconnect_title")}</h1>
          <p dangerouslySetInnerHTML={{ __html: I18n.t("how_to_connect_panel.done_disconnect_subtitle_html") }} />
          <br />
          <p className="cta">
            <a href="/apps" className="c-button">{I18n.t("how_to_connect_panel.back_to_apps")}</a>
          </p>
        </div>
      </div>
    );
  },

  renderDisconnectStep: function() {
    return (
      <div className="l-middle">
        <div className="mod-title">
          <h1>{I18n.t("how_to_connect_panel.disconnect_title", {app: this.props.app.name})}</h1>
        </div>

        <div className="mod-connect">
          <div className="box">
            <div className="content">
              <h2>{I18n.t("how_to_connect_panel.comments_title")}</h2>
              <p>{I18n.t("how_to_connect_panel.comments_description")}</p>
              <textarea valueLink={this.linkState("comments")} placeholder={I18n.t("how_to_connect_panel.comments_placeholder")} />
              <label>
                <input type="checkbox" checkedLink={this.linkState("accepted")} />

                {I18n.t("how_to_connect_panel.accept_disconnect", {app: this.props.app.name})}
              </label>
            </div>
          </div>
          <p className="cta">
            <a href="#" className={"c-button " + (this.state.accepted ? "" : "disabled")} onClick={this.handleDisconnect}>{I18n.t("how_to_connect_panel.disconnect")}</a>
          </p>
        </div>
      </div>
    );
  },

  handleGotoStep: function(step) {
    return function(e) {
      e.preventDefault();
      e.stopPropagation();
      this.setState({currentStep: step});
    }.bind(this);
  },

  handleMakeConnection: function() {
    if (this.state.accepted) {
      App.Controllers.Apps.makeConnection(this.props.app.id, this.state.comments, function() {
        this.setState({currentStep: "done"});
      }.bind(this));
    }
  },

  handleDisconnect: function() {
    if (this.state.accepted) {
      App.Controllers.Apps.disconnect(this.props.app.id, this.state.comments, function() {
        this.setState({currentStep: "done-disconnect"});
      }.bind(this));
    }
  }
});
