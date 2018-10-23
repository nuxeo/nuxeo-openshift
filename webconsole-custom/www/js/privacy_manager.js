'use strict';
/* eslint-env browser */
/* exported privacy_manager */ // no effect ("when the environment is node")?

var privacy_manager = (function () { // eslint-disable-line no-unused-vars
  var informed_cname = 'cookie_informed';
  var opted_out_cname = 'cookie_opted_out';

  var get_cookie_max_expiration_date = function () {
    var cookieTimeout = 33696000000; // today + 13 months = 13*30*24*60*60*1000
    var d = new Date();
    d.setTime(d.getTime() + cookieTimeout);
    return d.toGMTString();
  };

  var set_cookie = function (cname, cvalue, cpath) {
    var expires = 'expires=' + get_cookie_max_expiration_date();
    document.cookie = cname + '=' + cvalue + ';' + expires + ';path=' + cpath + ';domain=.nuxeo.com';
  };

  var set_informed_cookie = function () {
    set_cookie(informed_cname, 'true', '/');
  };

  var set_opted_in_cookie = function () {
    set_cookie(opted_out_cname, 'false', '/');
  };

  var set_opted_out_cookie = function () {
    set_cookie(opted_out_cname, 'true', '/');
  };

  var get_cookie = function (cname) {
    var name = cname + '=';
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) === ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) === 0) {
        return c.substring(name.length, c.length);
      }
    }
    return '';
  };

  var check_cookie = function (cname) {
    return get_cookie(cname) !== '';
  };

  var is_informed_cookie = function () {
    return get_cookie(informed_cname) === 'true';
  };

  var has_opted_out_cookie = function () {
    return check_cookie(opted_out_cname);
  };

  var is_opted_out_cookie = function () {
    return get_cookie(opted_out_cname) === 'true';
  };

  var delete_cookie = function (cname) {
    var hostname = document.location.hostname;
    if (hostname.indexOf('www.') === 0) {
      hostname = hostname.substring(3);
    }
    document.cookie = cname + '=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/;domain=' + hostname;
  };

  var delete_analytics_cookies = function () {
    var cnames = ['_ceg', '_ga', '_mkto', 'ajs_', 'intercom-'];
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) === ' ') {
        c = c.substring(1);
      }
      var end_index = c.indexOf('=');
      if (~end_index) {
        var cname = c.substring(0, end_index);
        for (var j = 0; j < cnames.length; j++) {
          if (cname.startsWith(cnames[j])) {
            delete_cookie(cname);
          }
        }
      }
    }
  };

  return {
    is_informed_cookie      : is_informed_cookie,
    set_informed_cookie     : set_informed_cookie,
    has_opted_out_cookie    : has_opted_out_cookie,
    is_opted_out_cookie     : is_opted_out_cookie,
    set_opted_in_cookie     : set_opted_in_cookie,
    set_opted_out_cookie    : set_opted_out_cookie,
    delete_analytics_cookies: delete_analytics_cookies
  };
}());
