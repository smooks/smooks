<!-- WML 2.0 Special Module  .................................... -->
<!-- file: wml-special-1.mod -->
<!-- 
    @Wireless Application Protocol Forum, Ltd. 2001.

	Terms and conditions of use are available from the Wireless Application Protocol Forum Ltd. 
	Web site (http://www.wapforum.org/what/copyright.htm).
-->
<!--
     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//WAPFORUM//ELEMENTS WML Special 1.0//EN"
       SYSTEM "wml-special-1.mod"

	This module contains declarations of the following WML elements. 
	
		card, do, onevent, access, go, prev,
		refresh, noop,  postfield, setvar, getvar
		timer, anchor
		
	It also contains WML extensions of XHTML elements. 
-->
<!-- ============================================ -->
<!-- 
	WML Global attributes
-->
<!-- ============================================ -->
<!-- 
	WML Event attributes
	
	These are used on the XHTML body and html elements.
	The same attributes are also available on the WML card element, but
	without namespace prefix.
-->
<!ENTITY % WML.event.attrib "%att.onenterforward.qname; CDATA	#IMPLIED
 %att.onenterbackward.qname; CDATA	#IMPLIED
 %att.ontimer.qname; CDATA	#IMPLIED">
<!-- ============================================ -->
<!-- 
	WML Elements
-->
<!-- ============================================ -->
<!-- 
	Cards 
	
	Use same content model as the HTML body element
-->
<!ELEMENT %card.qname; %CardBody.mix;>
<!ATTLIST %card.qname;
	%Common.attrib; 
	newcontext %Boolean.datatype; "false"
	onenterforward CDATA #IMPLIED
	onenterbackward CDATA #IMPLIED
	ontimer CDATA #IMPLIED
>
<!-- 
	Do 
-->
<!ELEMENT %do.qname; (%Tasks.class;)>
<!ATTLIST %do.qname;
	%Common.attrib; 
  	type             CDATA       #REQUIRED
  	label             CDATA       #IMPLIED
>
<!-- 
	Onevent 
-->
<!ELEMENT %onevent.qname; %Tasks.class;>
<!ATTLIST %onevent.qname;
	%Core.attrib; 
	type CDATA #REQUIRED
>
<!-- 
	Access 
-->
<!ELEMENT %access.qname; EMPTY>
<!ATTLIST %access.qname;
	%Core.attrib; 
	domain CDATA #IMPLIED
	path CDATA #IMPLIED
>
<!-- 
	Go 
-->
<!ENTITY % cache-control '(no-cache)'>
<!ELEMENT %go.qname; (%postfield.qname; | %setvar.qname;)*>
<!ATTLIST %go.qname;
	%Core.attrib; 
	href %URI.datatype; #REQUIRED
	sendreferer %Boolean.datatype; "false"
	type %ContentType.datatype; #IMPLIED
	method (post | get) "get"
	enctype %ContentType.datatype; "application/x-www-form-urlencoded"
	accept-charset CDATA #IMPLIED
	cache-control %cache-control; #IMPLIED
>
<!-- 
	Prev 
-->
<!ELEMENT %prev.qname; (%setvar.qname;)*>
<!ATTLIST %prev.qname;
	%Core.attrib; 
>
<!-- 
	Refresh 
-->
<!ELEMENT %refresh.qname; (%setvar.qname;)*>
<!ATTLIST %refresh.qname;
	%Core.attrib; 
>
<!-- 
	Noop 
-->
<!ELEMENT %noop.qname; EMPTY>
<!ATTLIST %noop.qname;
	%Core.attrib; 
>
<!-- 
	Postfield 
-->
<!ELEMENT %postfield.qname; EMPTY>
<!ATTLIST %postfield.qname;
	%Core.attrib; 
	name CDATA #REQUIRED
	value CDATA #REQUIRED
>
<!-- 
	Setvar 
-->
<!ELEMENT %setvar.qname; EMPTY>
<!ATTLIST %setvar.qname;
	%Core.attrib; 
	name CDATA #REQUIRED
	value CDATA #REQUIRED
>
<!-- 
	Getvar
-->
<!ELEMENT %getvar.qname; EMPTY>
<!ATTLIST %getvar.qname;
	%Core.attrib; 
	name CDATA #REQUIRED
	conversion (escape | noesc | unesc) "noesc"
>
<!-- 
	Timer  
-->
<!ELEMENT %timer.qname; EMPTY>
<!ATTLIST %timer.qname;
	%Core.attrib; 
	name NMTOKEN #IMPLIED
	value CDATA #REQUIRED
>
<!-- 
	Anchor 
	
	Same content model as HTML a element, but extended with WML tasks.
-->
<!ELEMENT %anchor.qname; (#PCDATA | %InlNoAnchor.mix; | %Tasks-nonoop.class;)*>
<!ATTLIST %anchor.qname;
	%Common.attrib; 
	accesskey CDATA #IMPLIED
>
<!-- ============================================ -->
<!-- 
	WML extensions of XHTML elements
-->
<!-- ============================================ -->
<!-- 
	select 
	
	*WML iname, ivalue, value, and name attributes
	*XHTML tabindex (not in Basic Forms module)
-->
<!ATTLIST %select.qname;
	%att.iname.qname; NMTOKEN #IMPLIED
	%att.value.qname; CDATA #IMPLIED
	%att.ivalue.qname; CDATA #IMPLIED
	%att.name.qname; CDATA #IMPLIED
>
<!-- 
	input 
	
	*WML format, emptyok, and name attributes
	*XHTML tabindex (not in Basic Forms module)
-->
<!ATTLIST %input.qname;
	%att.format.qname; CDATA #IMPLIED
	%att.emptyok.qname; %Boolean.datatype; #IMPLIED
	%att.name.qname; CDATA #IMPLIED
>
<!-- 
	textarea
	
	*WML format, emptyok, and name attributes
	*XHTML tabindex attribute (not in Basic Forms module)
-->
<!ATTLIST %textarea.qname;
	%att.format.qname; CDATA #IMPLIED
	%att.emptyok.qname; %Boolean.datatype; #IMPLIED
	%att.name.qname; CDATA #IMPLIED
>
<!-- 
	option
	
	*WML onpick attribute
-->
<!ATTLIST %option.qname;
	%att.onpick.qname; CDATA #IMPLIED
>
<!-- 
	img 

	*WML localsrc attribute
-->
<!ATTLIST img
	%att.localsrc.qname; CDATA #IMPLIED
	%att.type.qname; %ContentType.datatype; #IMPLIED
>
<!-- 
	table 
	
	*WML columns attribute
	
-->
<!ATTLIST %table.qname;
	%att.columns.qname; %Number.datatype; #IMPLIED
>
<!-- 
	body 
	
	*WML newcontext 
	*WML event attributes
-->
<!ATTLIST %body.qname;
	%att.newcontext.qname; %Boolean.datatype; "false"
	%WML.event.attrib; 
>
<!-- 
	html 
	
	*Declaration of WML namespace with prefix	
	*WML event attributes
-->
<!ATTLIST %html.qname;
	%WML.xmlns.extra.attrib; 
	%WML.event.attrib; 
>
