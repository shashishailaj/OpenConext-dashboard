import React from "react";
import I18n from "i18n-js";

import {AppShape} from "../shapes";

class SirtfiPanel extends React.Component {
  render() {
    return (
      <div className="l-middle">
        <div className="mod-title">
          <h1>{I18n.t("sirtfi_panel.title", {name: this.props.app.name})}</h1>

          <em className="info">{I18n.t("sirtfi_panel.subtitle")}</em>
        </div>
        {this.renderSirtfiContactPersons(this.props.app)}
      </div>
    );
  }

  renderSirtfiContactPersons(app) {
    return (
      <div className="mod-sirtfi">
        <table>
          <thead>
          <tr>
            <th>{I18n.t("sirtfi_panel.cp_name")}</th>
            <th>{I18n.t("sirtfi_panel.cp_email")}</th>
            <th>{I18n.t("sirtfi_panel.cp_telephoneNumber")}</th>
            <th>{I18n.t("sirtfi_panel.cp_type")}</th>
          </tr>
          </thead>
          <tbody>
          {app.contactPersons.map(this.renderContactPerson.bind(this))}
          </tbody>
        </table>
      </div>
    );
  }

  renderContactPerson(contactPerson, index) {
    return (
      <tr key={index}>
        <td>{contactPerson.name}</td>
        <td>{contactPerson.emailAddress}</td>
        <td>{contactPerson.telephoneNumber}</td>
        <td>{I18n.t("sirtfi_panel.cp_type_translate_"+contactPerson.contactPersonType)}</td>
      </tr>
    );
  }

}

SirtfiPanel.propTypes = {
  app: AppShape.isRequired
};

export default SirtfiPanel;
