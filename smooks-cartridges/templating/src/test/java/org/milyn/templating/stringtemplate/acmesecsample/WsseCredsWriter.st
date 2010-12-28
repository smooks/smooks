<wsse:Security xmlns:wsse="http://schemas.xmlsoap.org/ws/2002/04/secext">
    <wsse:UsernameToken>
        <wsse:Username>$credentials.username$</wsse:Username>
        <wsse:Password>$credentials.password$</wsse:Password>
    </wsse:UsernameToken>
</wsse:Security>
