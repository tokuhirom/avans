# avans

[![Build Status](https://travis-ci.org/tokuhirom/avans.svg?branch=master)](https://travis-ci.org/tokuhirom/avans)

Tiny and thin web application framework for Java 8.

## Motivation

I need tiny, thin, and simple web application framework for Java 8.
I need the web application framework like Sledge(Popular web application framework for Perl5).

## Architecture

You can build web application based on servlet API.
That's all.

## Components

### Core dependencies

 * mustache - very fast template engine.
 * jackson - really fast JSON serializer/deserializer
 * commons-fileupload - multipart/form-data processor

### And recommended modules

 * testmech - testing framework for web applications
 * tinyorm - Tiny O/R Mapper library

## FAQ

### Is there a HTML::FillInForm support?

No there isn't. You should do it with JavaScript.

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
