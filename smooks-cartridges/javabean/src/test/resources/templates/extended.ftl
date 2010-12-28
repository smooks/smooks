<?xml version="1.0" encoding="UTF-8"?>
<customers><#list customers as cust>
	<person>
		<surname>${cust.person.surname}</surname>
		<firstname>${cust.person.firstname}</firstname>
		<gender>${cust.person.gender}</gender>
		<phonenumber>${cust.person.phonenumber}</phonenumber>
	</person>
	<#list cust.addresses as addr>
	<address type="${addr.type}">
		<street>${addr.street}</street>
		<housenumber>${addr.housenumber}</housenumber>
		<zipcode>${addr.zipcode}</zipcode>
		<city>${addr.city}</city>
	</address>
	</#list>
	<orders>
		<#list cust.orders as ord>
		<order number="${ord.number}">
			<article id="${ord.article.id}">
				<name>${ord.article.name}</name>
				<price>${ord.article.price}</price>
			</article>
			<size>${ord.size}</size>
			<totalprice>${ord.price}</totalprice>
		</order>
		</#list>
	</orders>
</#list></customers>