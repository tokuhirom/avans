require 'minitest/autorun'
require 'httparty'
require 'thread'

class TestMeme < MiniTest::Test
  def test_run
    projdir = Dir.pwd
    tmpdir = Dir.mktmpdir

    Dir.chdir(tmpdir) do
      system(RbConfig.ruby, "#{projdir}/bin/avans_setup", 'com.example.foo') \
          or raise "Missing ruby"
      Dir.chdir('foo') do
        IO.popen(['mvn', 'exec:java', '-Dexec.mainClass=com.example.foo.Main', :err=>[:child, :out]]) do |io|
          io.each_line do |line|
            if line =~ /Started @/
              puts "Accessing to the server"
              response = HTTParty.get('http://127.0.0.1:21110')
              assert_equal response.code, 200
              assert_match /Hello/, response.body

              Process.kill(9, io.pid) # SIGKILL
            else
              puts "mvn: #{line}"
            end
          end
        end
      end
    end
  end
end
