SMS API
BULK SMS's Programmable SMS API enables you to programmatically send SMS messages from your web application. First, you need to create a new message object. BULK SMS returns the created message object with each request.
API Endpoint

Markup  PHP
https://bulksms.talksasa.com/api/v3/sms/send
Parameters
Parameter	Required	Description
Authorization	Yes	When calling our API, send your api token with the authentication type set as Bearer (Example: Authorization: Bearer {api_token})
Accept	Yes	Set to application/json
Send outbound SMS
BULK SMS's Programmable SMS API enables you to programmatically send SMS messages from your web application. First, you need to create a new message object. BULK SMS returns the created message object with each request.

Send your first SMS message with this example request.

API Endpoint

Markup
https://bulksms.talksasa.com/api/v3/sms/send
Parameters
Parameter	Required	Type	Description
recipient	Yes	string	Number to send message. Use comma (,) to send multiple numbers. Ex. 31612345678,880172145789
sender_id	Yes	string	The sender of the message. This can be a telephone number (including country code) or an alphanumeric string. In case of an alphanumeric string, the maximum length is 11 characters.
type	Yes	string	The type of the message. For text message you have to insert plain as sms type.
message	Yes	string	The body of the SMS message.
schedule_time	No	datetime	The scheduled date and time of the message in RFC3339 format (Y-m-d H:i)
dlt_template_id	No	string	The ID of your registered DLT (Distributed Ledger Technology) content template.
Example request for Single Number
PHP
curl -X POST https://bulksms.talksasa.com/api/v3/sms/send \
-H 'Authorization: Bearer 49|LNFe8WJ7CPtvl2mzowAB4ll4enbFR0XGgnQh2qWY' \
-H 'Content-Type: application/json' \
-H 'Accept: application/json' \
-d '{
"recipient":"31612345678",
"sender_id":"YourName",
"type":"plain",
"message":"This is a test message"
}'
Example request for Multiple Numbers
PHP
curl -X POST https://bulksms.talksasa.com/api/v3/sms/send \
  -H 'Authorization: Bearer 2|1qF0ry6pPJISiV8HYIciUekmiqAajvy8dkFN0d2T9c3d9c27' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
  "recipient":"254781000403,254707711847",
  "sender_id":"TALKSASA",
  "type":"plain",
  "message":"This is a test message",
  "schedule_time":"2021-12-20 07:00"
}'
Returns
Returns a contact object if the request was successful.

JSON
{
    "status": "success",
    "data": "sms reports with all details",
}
If the request failed, an error object will be returned.

JSON
{
    "status": "error",
    "message" : "A human-readable description of the error."
}
Send Campaign Using Contact list
BULK SMS's Programmable SMS API enables you to programmatically send Campaigns from your web application. First, you need to create a new message object. BULK SMS returns the created message object with each request.

Send your first Campaign Using Contact List with this example request.

API Endpoint

Markup
https://bulksms.talksasa.com/api/v3/sms/campaign
Parameters
Parameter	Required	Type	Description
contact_list_id	Yes	string	Contact list to send message. Use comma (,) to send multiple contact lists. Ex. 6415907d0d7a6,6415907d0d37a
sender_id	Yes	string	The sender of the message. This can be a telephone number (including country code) or an alphanumeric string. In case of an alphanumeric string, the maximum length is 11 characters.
type	Yes	string	The type of the message. For text message you have to insert plain as sms type.
message	Yes	string	The body of the SMS message.
schedule_time	No	datetime	The scheduled date and time of the message in RFC3339 format (Y-m-d H:i)
dlt_template_id	No	string	The ID of your registered DLT (Distributed Ledger Technology) content template.
Example request for Single Contact List
PHP
curl -X POST https://bulksms.talksasa.com/api/v3/sms/campaign \
-H 'Authorization: Bearer 49|LNFe8WJ7CPtvl2mzowAB4ll4enbFR0XGgnQh2qWY' \
-H 'Content-Type: application/json' \
-H 'Accept: application/json' \
-d '{
"recipient":"6415907d0d37a",
"sender_id":"YourName",
"type":"plain",
"message":"This is a test message"
}'
Example request for Multiple Contact Lists
PHP
curl -X POST https://bulksms.talksasa.com/api/v3/sms/campaign \
-H 'Authorization: Bearer 49|LNFe8WJ7CPtvl2mzowAB4ll4enbFR0XGgnQh2qWY' \
-H 'Content-Type: application/json' \
-H 'Accept: application/json' \
-d '{
"recipient":"6415907d0d37a,6415907d0d7a6",
"sender_id":"YourName",
"type":"plain",
"message":"This is a test message",
"schedule_time=2021-12-20 07:00"
}'
Returns
Returns a contact object if the request was successful.

JSON
{
    "status": "success",
    "data": "campaign reports with all details",
}
If the request failed, an error object will be returned.

JSON
{
    "status": "error",
    "message" : "A human-readable description of the error."
}
View an SMS
You can use BULK SMS's SMS API to retrieve information of an existing inbound or outbound SMS message.

You only need to supply the unique message id that was returned upon creation or receiving.

API Endpoint

Markup
https://bulksms.talksasa.com/api/v3/sms/{uid}
Parameters
Parameter	Required	Type	Description
uid	Yes	string	A unique random uid which is created on the BULK SMS platform and is returned upon creation of the object.
Example request
PHP
curl -X GET https://bulksms.talksasa.com/api/v3/sms/606812e63f78b \
-H 'Authorization: Bearer 49|LNFe8WJ7CPtvl2mzowAB4ll4enbFR0XGgnQh2qWY' \
-H 'Content-Type: application/json' \
-H 'Accept: application/json' \
Returns
Returns a contact object if the request was successful.

JSON
{
    "status": "success",
    "data": "sms data with all details",
}
If the request failed, an error object will be returned.

JSON
{
    "status": "error",
    "message" : "A human-readable description of the error."
}
View all messages
API Endpoint

Markup
https://bulksms.talksasa.com/api/v3/sms/
Example request
PHP
curl -X GET https://bulksms.talksasa.com/api/v3/sms \
-H 'Authorization: Bearer 49|LNFe8WJ7CPtvl2mzowAB4ll4enbFR0XGgnQh2qWY' \
-H 'Content-Type: application/json' \
-H 'Accept: application/json' \
Returns
Returns a contact object if the request was successful.

JSON
{
    "status": "success",
    "data": "sms reports with pagination",
}
If the request failed, an error object will be returned.

JSON
{
    "status": "error",
    "message" : "A human-readable description of the error."
}