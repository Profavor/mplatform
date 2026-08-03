export function formatMaskedInput(val: any, maskingPattern: string | null | undefined): any {
  if (!val || typeof val !== 'string' || !maskingPattern) return val;
  
  const pattern = maskingPattern.toUpperCase();
  
  if (pattern === 'RRN' || pattern === 'SSN') {
    let digits = val.replace(/[^\d*]/g, '').substring(0, 13);
    if (digits.length > 6) {
      return digits.substring(0, 6) + '-' + digits.substring(6);
    }
    return digits;
  } 
  
  if (pattern === 'PHONE' || pattern === 'MOBILE') {
    let digits = val.replace(/[^\d*]/g, '').substring(0, 11);
    if (digits.length > 7) {
      return digits.substring(0, 3) + '-' + digits.substring(3, 7) + '-' + digits.substring(7);
    } else if (digits.length > 3) {
      return digits.substring(0, 3) + '-' + digits.substring(3);
    }
    return digits;
  }
  
  if (pattern === 'CARD') {
    let digits = val.replace(/[^\d*]/g, '').substring(0, 16);
    const parts = [];
    for (let i = 0; i < digits.length; i += 4) {
      parts.push(digits.substring(i, Math.min(i + 4, digits.length)));
    }
    return parts.join('-');
  }

  return val;
}
