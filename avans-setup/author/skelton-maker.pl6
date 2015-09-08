#!/usr/bin/env perl6

use v6;
use File::Find;
use Shell::Command;

class Ho {
    has IO::Path $.srcdir;
    has IO::Path $.dstdir;
    has %!created_dirs;
    has @.files;

    method run() {
        self.copy-files();
        self.write-file-list();
    }

    method copy-files() {
        find(dir => $.srcdir, :type<file>).map: {
            self.process-file($_)
        };
    }

    method write-file-list() {
        my $fh = open("$.dstdir/FILELIST.txt", :w);
        for @.files { $fh.say($_) }
    }

    method process-file(IO::Path $file) {
        my $rel = $file.relative($.srcdir);
        if ($rel ~~ /^(build|\.gradle|\.idea|node_modules)\//) {
            return;
        }
        self.copy($file, $rel.IO);
    }

    method copy(IO::Path $file, IO::Path $rel) {
        my $dst = $!dstdir.child($rel.subst(/me\/geso/, '__groupId__', :g));
        self.mkpath($dst.dirname.IO);

        {
            say "cp $rel $dst";
            my $dat = slurp($file);
            # replace content
            my $content = $dat.subst(
                /sample/, '<<TMPL:artifactId>>', :g
            ).subst(
                /me\.geso/, '<<TMPL:groupId>>', :g
            );
            open($dst, :w).print($content);

            CATCH {
                when 'Malformed UTF-8' {
                    copy($file, $dst);
                }
                default { die $_ }
            }
        }

        @.files.push($dst);
    }

    method mkpath(IO::Path $dir) {
        return if %!created_dirs{$dir}++;
        dbg("mkdir -p $dir");
        mkpath($dir);
    }

    sub dbg($msg) {
        $msg.say if %*ENV<DEBUG>;
    }
}

sub MAIN($srcdir, $dstdir) {
    say "generate skelton from $srcdir. output directory is $dstdir.";

    my $ho = Ho.new(
        srcdir => $srcdir.IO,
        dstdir => $dstdir.IO
    );
    $ho.run();
}

