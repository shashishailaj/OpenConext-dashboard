import React from "react";
import I18n from "i18n-js";

import Spinner from "spin.js";
import spinner from "../lib/spin";

import Link from "react-router/Link";

class Navigation extends React.Component {
  constructor() {
    super();

    this.state = {
      loading: false
    };
  }

  componentWillMount() {
    spinner.onStart = () => this.setState({ loading: true });
    spinner.onStop = () => this.setState({ loading: false });
  }

  componentDidUpdate() {
    if (this.state.loading) {
      if (!this.spinner) {
        this.spinner = new Spinner({
          lines: 25, // The number of lines to draw
          length: 25, // The length of each line
          width: 4, // The line thickness
          radius: 20, // The radius of the inner circle
          color: "#4DB3CF", // #rgb or #rrggbb or array of colors
        }).spin(this.spinnerNode);
      }
    } else {
      this.spinner = null;
    }
  }

  render() {
    return (
      <div className="mod-navigation">
        <ul>
          {this.renderItem("/apps", "apps")}
          {this.renderItem("/policies", "policies")}
          {this.renderItem("/notifications", "notifications")}
          {this.renderItem("/history", "history")}
          {this.renderItem("/statistics", "stats")}
          {this.renderItem("/my-idp", "my_idp")}
        </ul>

        {this.renderSpinner()}
      </div>
    );
  }

  renderItem(href, value) {
    return <li><Link to={href} activeClassName="active">{I18n.t("navigation." + value)}</Link></li>;
  }

  renderSpinner() {
    if (this.state.loading) {
      return <div className="spinner" ref={spinner => this.spinnerNode = spinner}/>;
    }
    return null;
  }
}

export default Navigation;
