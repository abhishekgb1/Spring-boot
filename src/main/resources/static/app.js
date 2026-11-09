'use strict';
const form = document.querySelector('#conversion-form');
const amount = document.querySelector('#amount');
const from = document.querySelector('#from-currency');
const to = document.querySelector('#to-currency');
let currencies = [];
let busy = false;
const money = (value,code,source=false) => new Intl.NumberFormat(undefined,{style:'currency',currency:code,maximumFractionDigits:source?6:(code==='JPY'?0:2)}).format(value);
const decimal = value => new Intl.NumberFormat(undefined,{maximumFractionDigits:6}).format(value);
function setBusy(value) {
  busy = value;
  [amount,from,to,document.querySelector('#convert'),document.querySelector('#swap'),document.querySelector('#refresh'),document.querySelector('#retry')].forEach(control=>control.disabled=value);
  document.querySelector('#convert').textContent=value?'Converting…':'Convert currency ↗';
  form.setAttribute('aria-busy',String(value));
}
async function request(path,options={}) {
  const response=await fetch(path,{...options,signal:AbortSignal.timeout(20000)});
  const data=await response.json();
  if(!response.ok)throw new Error(data.message||'The currency service could not complete the request.');
  return data;
}
function clearResult() {
  document.querySelector('#result-input').textContent='Convert to see your result.';
  document.querySelector('#converted-amount').textContent='—';
  document.querySelector('#unit-rate').textContent='';
  document.querySelector('#error').hidden=true;
  document.querySelector('#amount-code').textContent=from.value;
  document.querySelector('#rates-body').replaceChildren();
  document.querySelector('#table-description').textContent='Convert an amount to see rates for your base currency.';
  document.querySelector('#timestamp').textContent='';
  document.querySelector('#data-status').textContent='Ready to convert.';
}
function showError(error) {
  clearResult();
  document.querySelector('#error').hidden=false;
  document.querySelector('#error-message').textContent=error.name==='TimeoutError'?'The request took too long. Please try again.':error.message;
  document.querySelector('#data-status').textContent='Rates unavailable. Try again when the service is reachable.';
}
async function convert() {
  if(busy||!form.reportValidity())return;
  setBusy(true);document.querySelector('#error').hidden=true;
  try {
    const [conversion,rates]=await Promise.all([
      request('/api/convert',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({amount:amount.value,fromCurrency:from.value,toCurrency:to.value})}),
      request('/api/rates/'+encodeURIComponent(from.value))
    ]);
    document.querySelector('#result-input').textContent=money(conversion.amount,conversion.fromCurrency,true)+' equals';
    document.querySelector('#converted-amount').textContent=money(conversion.convertedAmount,conversion.toCurrency);
    document.querySelector('#unit-rate').textContent='1 '+conversion.fromCurrency+' = '+decimal(conversion.rate)+' '+conversion.toCurrency;
    document.querySelector('#amount-code').textContent=from.value;
    document.querySelector('#table-description').textContent='Reference rates for 1 '+rates.baseCurrency+'.';
    document.querySelector('#rate-heading').textContent='Per 1 '+rates.baseCurrency;
    document.querySelector('#rates-body').replaceChildren(...currencies.filter(currency=>currency.code!==rates.baseCurrency).map(currency=>{
      const row=document.createElement('tr');
      for(const value of [currency.name,currency.code,decimal(rates.rates[currency.code])]){const cell=document.createElement('td');cell.textContent=value;row.append(cell);}return row;
    }));
    const stale=conversion.stale||rates.stale;
    document.querySelector('#data-status').classList.toggle('stale',stale);
    document.querySelector('#data-status').textContent=stale?'Cached rates — provider unavailable or update delayed.':'Daily reference rates · Latest available snapshot';
    document.querySelector('#timestamp').textContent='Provider updated: '+new Date(rates.updatedAt).toLocaleString();
  } catch(error) { showError(error); } finally { setBusy(false); }
}
async function initialize() {
  setBusy(true);
  try {
    currencies=await request('/api/currencies');
    for(const select of [from,to]) {select.replaceChildren(...currencies.map(currency=>{const option=document.createElement('option');option.value=currency.code;option.textContent=currency.code+' — '+currency.name;return option;}));}
    from.value='USD';to.value='INR';
    setBusy(false);await convert();
  } catch(error) {showError(error);setBusy(false);}
}
form.addEventListener('submit',event=>{event.preventDefault();convert();});
[amount,from,to].forEach(control=>control.addEventListener('input',clearResult));
document.querySelector('#swap').addEventListener('click',()=>{[from.value,to.value]=[to.value,from.value];clearResult();convert();});
document.querySelector('#refresh').addEventListener('click',()=>currencies.length?convert():initialize());
document.querySelector('#retry').addEventListener('click',()=>currencies.length?convert():initialize());
initialize();
