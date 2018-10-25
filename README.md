### MoneyTransfer 

This application is a test implementation of the RESTful API for money transfers between accounts.

For the purposes of testing it uses the in-memory H2 database out of the box as a datastore for accounts and transfers.

### Usage

Build service:

`mvn clean package`

Run service:

`java -Dport=<port> -Djdbc.url=<datastore_jdbc_url> [-Ddb.init.script=<schema_sql_script>] -jar money-transfer-<version>.jar`

Perform transfer:

`curl -X POST 'http://<host>:<port>/transfer/<account_id_from>/<account_id_to>?amount=<amount_of_money>`

The result of the call can be one of the following:
 - `status=200, body=<TransferID>` - money transferred
 - `status=400, body=<ErrorMessage>` - bad request
 - `status=404, body=404 Resource not found` - invalid url
 - `status=500, body=Internal server error` - internal server error