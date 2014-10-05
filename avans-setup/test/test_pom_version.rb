require 'minitest/autorun'
require 'httparty'

class TestMeme < MiniTest::Test
  # avans-setup must depended on latest avans.
  def test_project_version
    metaurl = 'http://tokuhirom.github.io/maven/releases/me/geso/avans/maven-metadata.xml'
    response = HTTParty.get(metaurl)
    assert_equal response.code, 200

    xml = response.body
    if xml =~ /<release>([^<>]+)<\/release>/
      version = $1
      assert File.read('./bin/avans-setup') =~ /#{version}/, "bin/avans-setup doesn't contains '#{version}'"
    else
      puts "maven-metadata.xml doesn't contains release tag."
    end
  end
end
