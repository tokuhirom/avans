# avans-mustache

[![Build Status](https://travis-ci.org/tokuhirom/avans-mustache.svg?branch=master)](https://travis-ci.org/tokuhirom/avans-mustache)

Mustache bindings for Avans web application framework.

## Avans?

Avans is a web application framework for Java 8.

## Mustache?

Mustache is a templating language.

## SYNOPSIS

	public static class MyController extends ControllerBase implements
			MustacheView {
		@GET("/mustache")
		public WebResponse mustache() {
			final Foo foo = new Foo();
			foo.setName("John");
			return this.renderMustache("mustache.mustache", foo);
		}
	}

Default view directory is `${basedir}/templates/`.

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
