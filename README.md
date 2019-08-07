# jmeetup-ssl
Demo SSL/TLS java spring boot app

##### SSL С ПРОВЕРКОЙ ДОМЕНА
##### SSL С ПРОВЕРКОЙ КОМПАНИИ
##### SSL С ЗЕЛЕНОЙ СТРОКОЙ
[Cert Types Doc](https://www.ispsystem.ru/ssl/choose)


add to server.xml (ibm liberty porofile) this
```
<feature>transportSecurity-1.0</feature>

 <keyStore id="defaultKeyStore" location="ClientKeyStore.p12" type="PKCS12" password="SECRET" />
 
 <keyStore id="defaultTrustStore" location="TrustStore.p12" type="PKCS12" password="SECRET" />
 
 <ssl id="defaultSSLConfig" sslProtocol="TLSv1.2" keyStoreRef="defaultKeyStore" trustStoreRef="defaultTrustStore" clientAuthenticationSupported="true" clientAuthentication="true" />
 
 <ssl id="controllerConnectionConfig" keyStoreRef="defaultKeyStore" trustStoreRef="defaultTrustStore" sslProtocol="TLSv1.2" />
 
 <ssl id="memberConnectionConfig" keyStoreRef="defaultKeyStore" trustStoreRef="defaultTrustStore" sslProtocol="TLSv1.2" />
```


Self sign cert chain can be generate by `openssl` Conf files in conf dir. Example:
```
openssl genrsa -out ca-privkey.pem 2048 
 
 openssl req -config ./ca.conf -new -x509 -key ca-privkey.pem -out cacert.pem -days 365
 
 openssl req -config ./server.conf -newkey rsa:2048 -days 365 -nodes -keyout server-key.pem -out server-req.pem
 
 openssl rsa -in server-key.pem -out server-key.pem
 
 openssl x509 -req -in server-req.pem -days 365 -CA cacert.pem -CAkey ca-privkey.pem -set_serial 01 -out server-cert.pem -extensions v3_req -extfile server.conf
 
 openssl req -config ./client.conf -newkey rsa:2048 -days 365 -nodes -keyout client-key.pem -out client-req.pem
 
 openssl rsa -in client-key.pem -out client-key.pem
 
 openssl x509 -req -in client-req.pem -days 365 -CA cacert.pem -CAkey ca-privkey.pem  -set_serial 01 -out client-cert.pem  -extensions v3_req -extfile client.conf
```