class AppBase {
    static DOMAIN = location.origin;

    static API_CUSTOMER = this.DOMAIN  + "/api/customers";
    static API_DEPOSIT = this.DOMAIN + "/api/deposits";
    static API_WITHDRAW = this.DOMAIN + "/api/withdraws";
    static API_TRANSFER = this.DOMAIN + "/api/transfers";
}