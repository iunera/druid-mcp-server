
openssl genpkey -algorithm Ed25519 -out key.pem
echo "iunera.com. IN TXT \"v=MCPv1; k=ed25519; p=$(openssl pkey -in key.pem -pubout -outform DER | tail -c 32 | base64)\""\n
./mcp-publisher login dns --domain iunera.com --private-key $(openssl pkey -in key.pem -noout -text | grep -A3 "priv:" | tail -n +2 | tr -d ' :\n')




# curl "https://registry.modelcontextprotocol.io/v0/servers?search=com.iunera/druid-mcp-server"

