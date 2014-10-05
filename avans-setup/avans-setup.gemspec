# coding: utf-8
lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'avans/setup/version'

Gem::Specification.new do |spec|
  spec.name          = "avans-setup"
  spec.version       = Avans::Setup::VERSION
  spec.authors       = ["Tokuhiro Matsuno"]
  spec.email         = ["tokuhirom@gmail.com"]
  spec.summary       = %q{Site skelton generator for avans}
  spec.description   = %q{Site skelton generator for avans}
  spec.homepage      = ""
  spec.license       = "MIT"

  spec.files         = `git ls-files -z`.split("\x0")
  spec.executables   = spec.files.grep(%r{^bin/}) { |f| File.basename(f) }
  spec.test_files    = spec.files.grep(%r{^(test|spec|features)/})
  spec.require_paths = ["lib"]

  spec.add_development_dependency "bundler", "~> 1.7"
  spec.add_development_dependency "rake", "~> 10.0"
  spec.add_development_dependency 'minitest', '~> 5.4.2'
end
