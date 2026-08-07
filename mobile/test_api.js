const http = require('http');

async function run() {
  const loginRes = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'password' })
  });
  const loginData = await loginRes.json();
  console.log('Login Response:', JSON.stringify(loginData, null, 2));
  const token = loginData.accessToken || loginData.token;
  console.log('Using Token:', token);

  const domainsRes = await fetch('http://localhost:8080/api/domains', {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  const domainsData = await domainsRes.json();
  console.log('Domains Data:', JSON.stringify(domainsData, null, 2));
  
  const domains = domainsData.content || domainsData.data || (Array.isArray(domainsData) ? domainsData : []);
  if (domains.length === 0) {
    console.log('No domains found!');
    return;
  }
  const domainId = domains[0].id;

  const recordsRes = await fetch(`http://localhost:8080/api/records/domain/${domainId}?page=0&size=20`, {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  const recordsData = await recordsRes.json();
  console.log('Records Response:', JSON.stringify(recordsData, null, 2));
}
run();
