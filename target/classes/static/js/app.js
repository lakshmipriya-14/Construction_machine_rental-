// BuildRight — main.js

// ── SIDEBAR TOGGLE ─────────────────────────────────────────
const sidebar  = document.getElementById('sidebar');
const overlay  = document.getElementById('overlay');
const hamburger = document.getElementById('hamburger');

function openSidebar()  { sidebar?.classList.add('open');  overlay?.classList.add('open'); }
function closeSidebar() { sidebar?.classList.remove('open'); overlay?.classList.remove('open'); }

hamburger?.addEventListener('click', openSidebar);
overlay?.addEventListener('click', closeSidebar);

// ── AUTO-DISMISS ALERTS ────────────────────────────────────
document.querySelectorAll('.alert[data-autohide]').forEach(el => {
  setTimeout(() => {
    el.style.transition = 'opacity .5s';
    el.style.opacity = '0';
    setTimeout(() => el.remove(), 500);
  }, 4000);
});

// ── CONFIRM DIALOGS ────────────────────────────────────────
document.querySelectorAll('[data-confirm]').forEach(el => {
  el.addEventListener('click', e => {
    if (!confirm(el.dataset.confirm)) e.preventDefault();
  });
});

// ── DYNAMIC FORM: delivery address reveal ─────────────────
const needsDelivery = document.getElementById('needsDelivery');
const addrGroup     = document.getElementById('deliveryAddrGroup');
if (needsDelivery && addrGroup) {
  const toggle = () => { addrGroup.style.display = needsDelivery.checked ? 'block' : 'none'; };
  needsDelivery.addEventListener('change', toggle);
  toggle();
}

// ── DYNAMIC FORM: for-sale price reveal ───────────────────
const forSaleCheck   = document.getElementById('forSale');
const salePriceGroup = document.getElementById('salePriceGroup');
if (forSaleCheck && salePriceGroup) {
  const toggle = () => { salePriceGroup.style.display = forSaleCheck.checked ? 'block' : 'none'; };
  forSaleCheck.addEventListener('change', toggle);
  toggle();
}

// ── COST ESTIMATOR (new rental form) ─────────────────────
const itemSelect   = document.getElementById('itemId');
const startInput   = document.getElementById('startDate');
const endInput     = document.getElementById('endDate');
const qtyInput     = document.getElementById('quantity');
const costDisplay  = document.getElementById('costEstimate');

function updateCostEstimate() {
  if (!itemSelect || !startInput.value || !endInput.value) return;
  const opt  = itemSelect.options[itemSelect.selectedIndex];
  if (!opt || !opt.dataset.rate) return;
  const rate = parseFloat(opt.dataset.rate) || 0;
  const delFee = parseFloat(opt.dataset.delfee) || 0;
  const qty  = parseInt(qtyInput?.value) || 1;
  const [d1, m1, y1] = startInput.value.split('/');
  const [d2, m2, y2] = endInput.value.split('/');
  const start = new Date(`${y1}-${m1}-${d1}`);
  const end   = new Date(`${y2}-${m2}-${d2}`);
  const days  = Math.max(0, Math.round((end - start) / 86400000));
  const base  = rate * qty * days;
  const needsDel = document.getElementById('needsDelivery')?.checked;
  const total = base + (needsDel ? delFee : 0);
  if (costDisplay && days > 0) {
    costDisplay.innerHTML =
      `<strong>${days} day(s)</strong> × ₱${rate.toLocaleString()}/day × ${qty} unit(s) = ` +
      `<span class="text-primary fw-bold">₱${total.toLocaleString('en-PH', {minimumFractionDigits:2})}</span>` +
      (needsDel ? ` <span class="text-muted fs-sm">(+ ₱${delFee.toLocaleString()} delivery)</span>` : '');
    costDisplay.parentElement.style.display = 'block';
  } else if (costDisplay) {
    costDisplay.parentElement.style.display = 'none';
  }
}

[itemSelect, startInput, endInput, qtyInput].forEach(el => {
  el?.addEventListener('change', updateCostEstimate);
  el?.addEventListener('input', updateCostEstimate);
});
document.getElementById('needsDelivery')?.addEventListener('change', updateCostEstimate);

// ── NUMBER FORMAT HELPER ───────────────────────────────────
document.querySelectorAll('[data-currency]').forEach(el => {
  const val = parseFloat(el.dataset.currency);
  if (!isNaN(val)) el.textContent = '₱' + val.toLocaleString('en-PH', {minimumFractionDigits:2, maximumFractionDigits:2});
});
