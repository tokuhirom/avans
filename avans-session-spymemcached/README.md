# avans-session-spymemcached

[![Build Status](https://travis-ci.org/tokuhirom/avans-session-spymemcached.svg?branch=master)](https://travis-ci.org/tokuhirom/avans-session-spymemcached)

Avans' session storage library using spymemcached.
spymemcached is a popular implementation of memcached client library.

## SYNOPSIS

    public static class MyController extends ControllerBase implements
        SessionMixin {
      private static final SecretKeySpec signingKey = new SecretKeySpec("My Secret".getBytes(), "HmacSHA1");

      public MemcachedClient buildMemcachedClient() {
        final InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 11211);
        return new MemcachedClient(addr);
      }

      @Override
      public WebSessionManager buildSessionManager() {
        try {
          final SpyMemcachedSessionStore sessionStore = new SpyMemcachedSessionStore(
              this.buildMemcachedClient(), 1024);
          final Mac xsrfTokenMac = Mac.getInstance("HmacSHA1");
          xsrfTokenMac.init(signingKey);
          return new DefaultWebSessionManager(
              "avans_session_id",
              this.getRequest(),
              sessionStore,
              xsrfTokenMac);
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
          throw new RuntimeException(e);
        }
      }
    }

## LICENSE

    The MIT License (MIT)
    Copyright © 2014 Tokuhiro Matsuno, http://64p.org/ <tokuhirom@gmail.com>

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the “Software”), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
