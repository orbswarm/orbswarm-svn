

function trace(msg, blank)
{
   //jelli.DEBUGGING = true;
    //if (!jelli.DEBUGGING) { return; }


    //if ((BrowserDetect.browser == 'Firefox' || BrowserDetect.browser == 'Safari') && window.console) {
	if (window.console && console.log) {
        console.log(msg); //console is the firebug debugging display that can also handle objects
    }
};


var hexcase=0;
var b64pad="";
var chrsz=8;

function hex_md5(s) {
   return binl2hex(core_md5(str2binl(s),s.length*chrsz));
}

function b64_md5(s){
   return binl2b64(core_md5(str2binl(s),s.length*chrsz));
}

function str_md5(s) {
   return binl2str(core_md5(str2binl(s),s.length*chrsz));
}

function hex_hmac_md5(key,data) {
   return binl2hex(core_hmac_md5(key,data));
} 

function b64_hmac_md5(key,data){
   return binl2b64(core_hmac_md5(key,data));
}

function str_hmac_md5(key,data){
   return binl2str(core_hmac_md5(key,data));
}

function md5_vm_test(){
   return hex_md5("abc")=="900150983cd24fb0d6963f7d28e17f72";
}

function core_md5(x,len){
   x[len>>5]|=0x80<<((len)%32);
   x[(((len+64)>>>9)<<4)+14]=len;
   var a=1732584193;
   var b=-271733879;
   var c=-1732584194;
   var d=271733878;
   for(var i=0;i<x.length;i+=16){
      var olda=a;
      var oldb=b;
      var oldc=c;
      var oldd=d;
      a=md5_ff(a,b,c,d,x[i+0],7,-680876936);
      d=md5_ff(d,a,b,c,x[i+1],12,-389564586);
      c=md5_ff(c,d,a,b,x[i+2],17,606105819);
      b=md5_ff(b,c,d,a,x[i+3],22,-1044525330);
      a=md5_ff(a,b,c,d,x[i+4],7,-176418897);
      d=md5_ff(d,a,b,c,x[i+5],12,1200080426);
      c=md5_ff(c,d,a,b,x[i+6],17,-1473231341);
      b=md5_ff(b,c,d,a,x[i+7],22,-45705983);
      a=md5_ff(a,b,c,d,x[i+8],7,1770035416);
      d=md5_ff(d,a,b,c,x[i+9],12,-1958414417);
      c=md5_ff(c,d,a,b,x[i+10],17,-42063);
      b=md5_ff(b,c,d,a,x[i+11],22,-1990404162);
      a=md5_ff(a,b,c,d,x[i+12],7,1804603682);
      d=md5_ff(d,a,b,c,x[i+13],12,-40341101);
      c=md5_ff(c,d,a,b,x[i+14],17,-1502002290);
      b=md5_ff(b,c,d,a,x[i+15],22,1236535329);
      a=md5_gg(a,b,c,d,x[i+1],5,-165796510);
      d=md5_gg(d,a,b,c,x[i+6],9,-1069501632);
      c=md5_gg(c,d,a,b,x[i+11],14,643717713);
      b=md5_gg(b,c,d,a,x[i+0],20,-373897302);
      a=md5_gg(a,b,c,d,x[i+5],5,-701558691);
      d=md5_gg(d,a,b,c,x[i+10],9,38016083);
      c=md5_gg(c,d,a,b,x[i+15],14,-660478335);
      b=md5_gg(b,c,d,a,x[i+4],20,-405537848);
      a=md5_gg(a,b,c,d,x[i+9],5,568446438);
      d=md5_gg(d,a,b,c,x[i+14],9,-1019803690);
      c=md5_gg(c,d,a,b,x[i+3],14,-187363961);
      b=md5_gg(b,c,d,a,x[i+8],20,1163531501);
      a=md5_gg(a,b,c,d,x[i+13],5,-1444681467);
      d=md5_gg(d,a,b,c,x[i+2],9,-51403784);
      c=md5_gg(c,d,a,b,x[i+7],14,1735328473);
      b=md5_gg(b,c,d,a,x[i+12],20,-1926607734);
      a=md5_hh(a,b,c,d,x[i+5],4,-378558);
      d=md5_hh(d,a,b,c,x[i+8],11,-2022574463);
      c=md5_hh(c,d,a,b,x[i+11],16,1839030562);
      b=md5_hh(b,c,d,a,x[i+14],23,-35309556);
      a=md5_hh(a,b,c,d,x[i+1],4,-1530992060);
      d=md5_hh(d,a,b,c,x[i+4],11,1272893353);
      c=md5_hh(c,d,a,b,x[i+7],16,-155497632);
      b=md5_hh(b,c,d,a,x[i+10],23,-1094730640);
      a=md5_hh(a,b,c,d,x[i+13],4,681279174);
      d=md5_hh(d,a,b,c,x[i+0],11,-358537222);
      c=md5_hh(c,d,a,b,x[i+3],16,-722521979);
      b=md5_hh(b,c,d,a,x[i+6],23,76029189);
      a=md5_hh(a,b,c,d,x[i+9],4,-640364487);
      d=md5_hh(d,a,b,c,x[i+12],11,-421815835);
      c=md5_hh(c,d,a,b,x[i+15],16,530742520);
      b=md5_hh(b,c,d,a,x[i+2],23,-995338651);
      a=md5_ii(a,b,c,d,x[i+0],6,-198630844);
      d=md5_ii(d,a,b,c,x[i+7],10,1126891415);
      c=md5_ii(c,d,a,b,x[i+14],15,-1416354905);
      b=md5_ii(b,c,d,a,x[i+5],21,-57434055);
      a=md5_ii(a,b,c,d,x[i+12],6,1700485571);
      d=md5_ii(d,a,b,c,x[i+3],10,-1894986606);
      c=md5_ii(c,d,a,b,x[i+10],15,-1051523);
      b=md5_ii(b,c,d,a,x[i+1],21,-2054922799);
      a=md5_ii(a,b,c,d,x[i+8],6,1873313359);
      d=md5_ii(d,a,b,c,x[i+15],10,-30611744);
      c=md5_ii(c,d,a,b,x[i+6],15,-1560198380);
      b=md5_ii(b,c,d,a,x[i+13],21,1309151649);
      a=md5_ii(a,b,c,d,x[i+4],6,-145523070);
      d=md5_ii(d,a,b,c,x[i+11],10,-1120210379);
      c=md5_ii(c,d,a,b,x[i+2],15,718787259);
      b=md5_ii(b,c,d,a,x[i+9],21,-343485551);
      a=safe_add(a,olda);
      b=safe_add(b,oldb);
      c=safe_add(c,oldc);
      d=safe_add(d,oldd);
      }
   return Array(a,b,c,d);
}

function md5_cmn(q,a,b,x,s,t){
   return safe_add(bit_rol(safe_add(safe_add(a,q),safe_add(x,t)),s),b);
}

function md5_ff(a,b,c,d,x,s,t){
   return md5_cmn((b&c)|((~b)&d),a,b,x,s,t);
}

function md5_gg(a,b,c,d,x,s,t){
   return md5_cmn((b&d)|(c&(~d)),a,b,x,s,t);
}

function md5_hh(a,b,c,d,x,s,t){
   return md5_cmn(b^c^d,a,b,x,s,t);
}

function md5_ii(a,b,c,d,x,s,t){
   return md5_cmn(c^(b|(~d)),a,b,x,s,t);
}

function core_hmac_md5(key,data){
  var bkey=str2binl(key);
  if(bkey.length>16)bkey=core_md5(bkey,key.length*chrsz);
  var ipad=Array(16),opad=Array(16);
  for(var i=0;i<16;i++){
     ipad[i]=bkey[i]^0x36363636;
     opad[i]=bkey[i]^0x5C5C5C5C;
  }
  var hash=core_md5(ipad.concat(str2binl(data)),512+data.length*chrsz);
  return core_md5(opad.concat(hash),512+128);
}

function safe_add(x,y){
  var lsw=(x&0xFFFF)+(y&0xFFFF);
  var msw=(x>>16)+(y>>16)+(lsw>>16);
  return(msw<<16)|(lsw&0xFFFF);
}

function bit_rol(num,cnt){
   return(num<<cnt)|(num>>>(32-cnt));
}

function str2binl(str){
   var bin=Array();
   var mask=(1<<chrsz)-1;
   for(var i=0;i<str.length*chrsz;i+=chrsz) bin[i>>5]|=(str.charCodeAt(i/chrsz)&mask)<<(i%32);
   return bin;
}

function binl2str(bin){
   var str="";
   var mask=(1<<chrsz)-1;
   for(var i=0;i<bin.length*32;i+=chrsz)str+=String.fromCharCode((bin[i>>5]>>>(i%32))&mask);
   return str;
}

function binl2hex(binarray){
   var hex_tab=hexcase?"0123456789ABCDEF":"0123456789abcdef";
   var str="";
   for(var i=0;i<binarray.length*4;i++) {
      str+=hex_tab.charAt((binarray[i>>2]>>((i%4)*8+4))&0xF)+hex_tab.charAt((binarray[i>>2]>>((i%4)*8))&0xF);
   }
   return str;
}

function binl2b64(binarray){
   var tab="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
   var str="";
   for(var i=0;i<binarray.length*4;i+=3){
      var triplet=(((binarray[i>>2]>>8*(i%4))&0xFF)<<16)|(((binarray[i+1>>2]>>8*((i+1)%4))&0xFF)<<8)|((binarray[i+2>>2]>>8*((i+2)%4))&0xFF);
      for(var j=0;j<4;j++){
         if (i*8+j*6>binarray.length*32)
            str+=b64pad;
         else str+=tab.charAt((triplet>>6*(3-j))&0x3F);
      }
   }
   return str;
}

function strpos( haystack, needle, offset){
    var i = (haystack+'').indexOf( needle, offset ); 
    return i===-1 ? false : i;
}


function oc(a)
{
  var o = {};
  for(var i=0;i<a.length;i++)
  {
    o[a[i]]='';
  }
  return o;
}

//crockford's purge
function purge(d) { 
    var a = d.attributes, i, l, n;
    if (a) {
        l = a.length;
        for (i = 0; i < l; i += 1) {
            n = a[i].name;
            if (typeof d[n] === 'function') {
                d[n] = null;
            }
        }
    }
    a = d.childNodes;
    if (a) {
        l = a.length;
        for (i = 0; i < l; i += 1) {
            purge(d.childNodes[i]);
        }
    }
}

//resig's wbr
function wbr(str, num) { 
	//if (BrowserDetect.browser != 'Explorer') {
	if (!$.browser.msie) {	
  		return str.replace(RegExp("(\\w{" + num + "})(\\w)", "g"), function(all,t,c){
    		var returnVal = t + "<wbr>" + c;
			return returnVal;
  		});
	} else {
		return(str);
	}
}

//php .js html entities
function get_html_translation_table(table, quote_style) {
    // http://kevin.vanzonneveld.net
    // +   original by: Philip Peterson
    // +    revised by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +   bugfixed by: noname
    // +   bugfixed by: Alex
    // +   bugfixed by: Marco
    // %          note: It has been decided that we're not going to add global
    // %          note: dependencies to php.js. Meaning the constants are not
    // %          note: real constants, but strings instead. integers are also supported if someone
    // %          note: chooses to create the constants themselves.
    // %          note: Table from http://www.the-art-of-web.com/html/character-codes/
    // *     example 1: get_html_translation_table('HTML_SPECIALCHARS');
    // *     returns 1: {'"': '&quot;', '&': '&amp;', '<': '&lt;', '>': '&gt;'}
    
    var entities = {}, histogram = {}, decimal = 0, symbol = '';
    var constMappingTable = {}, constMappingQuoteStyle = {};
    var useTable = {}, useQuoteStyle = {};
    
    useTable      = (table ? table.toUpperCase() : 'HTML_SPECIALCHARS');
    useQuoteStyle = (quote_style ? quote_style.toUpperCase() : 'ENT_COMPAT');
    
    // Translate arguments
    constMappingTable[0]      = 'HTML_SPECIALCHARS';
    constMappingTable[1]      = 'HTML_ENTITIES';
    constMappingQuoteStyle[0] = 'ENT_NOQUOTES';
    constMappingQuoteStyle[2] = 'ENT_COMPAT';
    constMappingQuoteStyle[3] = 'ENT_QUOTES';
    
    // Map numbers to strings for compatibilty with PHP constants
    if (!isNaN(useTable)) {
        useTable = constMappingTable[useTable];
    }
    if (!isNaN(useQuoteStyle)) {
        useQuoteStyle = constMappingQuoteStyle[useQuoteStyle];
    }
    
    if (useQuoteStyle != 'ENT_NOQUOTES') {
        entities['34'] = '&quot;';
    }
 
    if (useQuoteStyle == 'ENT_QUOTES') {
        entities['39'] = '&#039;';
    }
 
    if (useTable == 'HTML_SPECIALCHARS') {
        // ascii decimals for better compatibility
        entities['38'] = '&amp;';
        entities['60'] = '&lt;';
        entities['62'] = '&gt;';
    } else if (useTable == 'HTML_ENTITIES') {
        // ascii decimals for better compatibility
      entities['38']  = '&amp;';
      entities['60']  = '&lt;';
      entities['62']  = '&gt;';
      entities['160'] = '&nbsp;';
      entities['161'] = '&iexcl;';
      entities['162'] = '&cent;';
      entities['163'] = '&pound;';
      entities['164'] = '&curren;';
      entities['165'] = '&yen;';
      entities['166'] = '&brvbar;';
      entities['167'] = '&sect;';
      entities['168'] = '&uml;';
      entities['169'] = '&copy;';
      entities['170'] = '&ordf;';
      entities['171'] = '&laquo;';
      entities['172'] = '&not;';
      entities['173'] = '&shy;';
      entities['174'] = '&reg;';
      entities['175'] = '&macr;';
      entities['176'] = '&deg;';
      entities['177'] = '&plusmn;';
      entities['178'] = '&sup2;';
      entities['179'] = '&sup3;';
      entities['180'] = '&acute;';
      entities['181'] = '&micro;';
      entities['182'] = '&para;';
      entities['183'] = '&middot;';
      entities['184'] = '&cedil;';
      entities['185'] = '&sup1;';
      entities['186'] = '&ordm;';
      entities['187'] = '&raquo;';
      entities['188'] = '&frac14;';
      entities['189'] = '&frac12;';
      entities['190'] = '&frac34;';
      entities['191'] = '&iquest;';
      entities['192'] = '&Agrave;';
      entities['193'] = '&Aacute;';
      entities['194'] = '&Acirc;';
      entities['195'] = '&Atilde;';
      entities['196'] = '&Auml;';
      entities['197'] = '&Aring;';
      entities['198'] = '&AElig;';
      entities['199'] = '&Ccedil;';
      entities['200'] = '&Egrave;';
      entities['201'] = '&Eacute;';
      entities['202'] = '&Ecirc;';
      entities['203'] = '&Euml;';
      entities['204'] = '&Igrave;';
      entities['205'] = '&Iacute;';
      entities['206'] = '&Icirc;';
      entities['207'] = '&Iuml;';
      entities['208'] = '&ETH;';
      entities['209'] = '&Ntilde;';
      entities['210'] = '&Ograve;';
      entities['211'] = '&Oacute;';
      entities['212'] = '&Ocirc;';
      entities['213'] = '&Otilde;';
      entities['214'] = '&Ouml;';
      entities['215'] = '&times;';
      entities['216'] = '&Oslash;';
      entities['217'] = '&Ugrave;';
      entities['218'] = '&Uacute;';
      entities['219'] = '&Ucirc;';
      entities['220'] = '&Uuml;';
      entities['221'] = '&Yacute;';
      entities['222'] = '&THORN;';
      entities['223'] = '&szlig;';
      entities['224'] = '&agrave;';
      entities['225'] = '&aacute;';
      entities['226'] = '&acirc;';
      entities['227'] = '&atilde;';
      entities['228'] = '&auml;';
      entities['229'] = '&aring;';
      entities['230'] = '&aelig;';
      entities['231'] = '&ccedil;';
      entities['232'] = '&egrave;';
      entities['233'] = '&eacute;';
      entities['234'] = '&ecirc;';
      entities['235'] = '&euml;';
      entities['236'] = '&igrave;';
      entities['237'] = '&iacute;';
      entities['238'] = '&icirc;';
      entities['239'] = '&iuml;';
      entities['240'] = '&eth;';
      entities['241'] = '&ntilde;';
      entities['242'] = '&ograve;';
      entities['243'] = '&oacute;';
      entities['244'] = '&ocirc;';
      entities['245'] = '&otilde;';
      entities['246'] = '&ouml;';
      entities['247'] = '&divide;';
      entities['248'] = '&oslash;';
      entities['249'] = '&ugrave;';
      entities['250'] = '&uacute;';
      entities['251'] = '&ucirc;';
      entities['252'] = '&uuml;';
      entities['253'] = '&yacute;';
      entities['254'] = '&thorn;';
      entities['255'] = '&yuml;';
    } else {
        throw Error("Table: "+useTable+' not supported');
        return false;
    }
    
    // ascii decimals to real symbols
    for (decimal in entities) {
        symbol = String.fromCharCode(decimal);
        histogram[symbol] = entities[decimal];
    }
    
    return histogram;
}

function htmlentities (string, quote_style) {
    // http://kevin.vanzonneveld.net
    // +   original by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +    revised by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +   improved by: nobbler
    // +    tweaked by: Jack
    // +   bugfixed by: Onno Marsman
    // +    revised by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // -    depends on: get_html_translation_table
    // *     example 1: htmlentities('Kevin & van Zonneveld');
    // *     returns 1: 'Kevin &amp; van Zonneveld'
 
    var histogram = {}, symbol = '', tmp_str = '', entity = '';
    tmp_str = string.toString();
    
    if (false === (histogram = get_html_translation_table('HTML_ENTITIES', quote_style))) {
        return false;
    }
    
    for (symbol in histogram) {
        entity = histogram[symbol];
        tmp_str = tmp_str.split(symbol).join(entity);
    }
    
    return tmp_str;
}
function htmlspecialchars (string, quote_style) {
    // http://kevin.vanzonneveld.net
    // +   original by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +    revised by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +   improved by: nobbler
    // +    tweaked by: Jack
    // +   bugfixed by: Onno Marsman
    // +    revised by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // -    depends on: get_html_translation_table
    // *     example 1: htmlentities('Kevin & van Zonneveld');
    // *     returns 1: 'Kevin &amp; van Zonneveld'
 
    var histogram = {}, symbol = '', tmp_str = '', entity = '';
    tmp_str = string.toString();
    
    if (false === (histogram = get_html_translation_table('HTML_SPECIALCHARS', quote_style))) {
        return false;
    }
    
    for (symbol in histogram) {
        entity = histogram[symbol];
        tmp_str = tmp_str.split(symbol).join(entity);
    }
    
    return tmp_str;
}

/*
function areArraysEqual(array1, array2) {
   var temp = new Array();
   if ( (!array1[0]) || (!array2[0]) ) { // If either is not an array
      return false;
   }
   if (array1.length != array2.length) {
      return false;
   }
   // Put all the elements from array1 into a "tagged" array
   for (var i=0; i<array1.length; i++) {
      key = (typeof array1[i]) + "~" + array1[i];
   // Use "typeof" so a number 1 isn't equal to a string "1".
      if (temp[key]) { temp[key]++; } else { temp[key] = 1; }
   // temp[key] = # of occurrences of the value (so an element could appear multiple times)
   }
   // Go through array2 - if same tag missing in "tagged" array, not equal
   for (var i=0; i<array2.length; i++) {
      key = (typeof array2[i]) + "~" + array2[i];
      if (temp[key]) {
         if (temp[key] == 0) { return false; } else { temp[key]--; }
      // Subtract to keep track of # of appearances in array2
      } else { // Key didn't appear in array1, arrays are not equal.
         return false;
      }
   }
   // If we get to this point, then every generated key in array1 showed up the exact same
   // number of times in array2, so the arrays are equal.
   return true;
}
*/

function areArraysEqual(array1, array2) {
	if ( (!array1[0]) || (!array2[0]) ) { // If either is not an array
		return false;
	}
	if (array1.length != array2.length) {
		return false;
	}
	for (var i=0; i<array1.length; i++) {
		if (array1[i] != array2[i]) {
			return false;
		}
	}
	return true;
}

Array.prototype.findIndex = function(value){
var ctr = "";
for (var i=0; i < this.length; i++) {
// use === to check for Matches. ie., identical (===), ;
if (this[i] == value) {
return i;
}
}
return ctr;
};

// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
  var rest = this.slice((to || from) + 1 || this.length);
  this.length = from < 0 ? this.length + from : from;
  return this.push.apply(this, rest);
};

if (typeof(time) != "function") {
	function time(timerName) {
		//if(!window.console||window.console&&!window.console.firebug) {
		if (window.console && console.time) {
			console.time(timerName);
		}
		//}
	}
}
if (typeof(timeEnd) != "function") {
	function timeEnd(timerName) {
		//if(!window.console||window.console&&!window.console.firebug) {
		if (window.console && console.timeEnd) {
			console.timeEnd(timerName);
		}
	}
}
Array.prototype.removeItems = function(itemsToRemove) {

    if (!/Array/.test(itemsToRemove.constructor)) {
        itemsToRemove = [ itemsToRemove ];
    }

    var j;
    for (var i = 0; i < itemsToRemove.length; i++) {
        j = 0;
        while (j < this.length) {
            if (this[j] == itemsToRemove[i]) {
                this.splice(j, 1);
            } else {
                j++;
            }
        }
    }
}
function toHex(dec) {
        // create list of hex characters
        var hexCharacters = "0123456789ABCDEF"
        // if number is out of range return limit
        if (dec < 0)
                return "00"
        if (dec > 255)
                return "FF"
        // decimal equivalent of first hex character in converted number
        var i = Math.floor(dec / 16)
        // decimal equivalent of second hex character in converted number
        var j = dec % 16
        // return hexadecimal equivalent
        return hexCharacters.charAt(i) + hexCharacters.charAt(j)
}
