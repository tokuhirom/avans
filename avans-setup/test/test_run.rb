require 'minitest/autorun'
require 'httparty'

class TestMeme < MiniTest::Test
  def runit(*cmd)
    puts cmd.join(' ')
    unless system(*cmd)
      status = $?.exitstatus
      unless status == 0
        fail "Bad exit status: #{status}"
      end
    end
  end

  def test_run
    projdir = Dir.pwd

    Dir.mktmpdir() do |tmpdir|
      Dir.chdir(tmpdir) do
        assert runit(RbConfig.ruby, "#{projdir}/bin/avans_setup", 'com.example.foo')
        Dir.chdir('foo') do
          IO.popen(['mvn', 'exec:java', '-Dexec.mainClass=com.example.foo.Main', :err=>[:child, :out]]) do |io|
            io.each_line do |line|
              if line =~ /Started @/
                puts "Accessing to the server"
                response = HTTParty.get('http://127.0.0.1:21110')
                assert_equal response.code, 200
                assert_match(/Hello/, response.body)

                Process.kill(9, io.pid) # SIGKILL
              else
                puts "mvn: #{line}"
              end
            end
          end
          assert system('mvn', 'test'), 'mvn test'
        end
      end
    end
  end
end
