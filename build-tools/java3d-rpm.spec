Summary: Java 3D @VERSION@
Name: java3d
Version:@VERSION_RPM@
Release: 1
Copyright: JDL
Group: System Environment/Base
BuildRoot: /var/tmp/%{name}-buildroot
Source:java3d-@VERSION_FILE@.tar.gz
Prefix:/usr/java/jdk1.5.0

%description
Java 3D @VERSION@ API

%prep
%setup -c

%build

%install
mkdir -p $RPM_BUILD_ROOT%prefix/jre/lib/ext
mkdir -p $RPM_BUILD_ROOT%prefix/jre/lib/i386

#install -s -m 755 lib/ext/j3daudio.jar $RPM_BUILD_ROOT%prefix/jre/lib/ext
install -s -m 755 lib/ext/j3dcore.jar $RPM_BUILD_ROOT%prefix/jre/lib/ext
install -s -m 755 lib/ext/j3dutils.jar $RPM_BUILD_ROOT%prefix/jre/lib/ext
install -s -m 755 lib/ext/vecmath.jar $RPM_BUILD_ROOT%prefix/jre/lib/ext
#install -s -m 755 lib/i386/libj3daudio.so $RPM_BUILD_ROOT%prefix/jre/lib/i386
install -s -m 755 lib/i386/libj3dcore-ogl.so $RPM_BUILD_ROOT%prefix/jre/lib/i386
install -s -m 755 lib/i386/libj3dutils.so $RPM_BUILD_ROOT%prefix/jre/lib/i386
#install -s -m 755 BINARY-CODE-LICENSE.txt $RPM_BUILD_ROOT%prefix

#install -s -m 755 java3d-utils-src.jar $RPM_BUILD_ROOT%prefix/
#install -s -m 755 java3d-demo.tar.gz $RPM_BUILD_ROOT%prefix/


%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(755,root,root)
#/usr/java/jdk1.5.0/jre/lib/ext/j3daudio.jar
%prefix/jre/lib/ext/j3dcore.jar
%prefix/jre/lib/ext/j3dutils.jar
%prefix/jre/lib/ext/vecmath.jar
#/usr/java/jdk1.5.0/jre/lib/i386/libj3daudio.so
%prefix/jre/lib/i386/libj3dcore-ogl.so
%prefix/jre/lib/i386/libj3dutils.so
#/usr/java/jdk1.5.0/BINARY-CODE-LICENSE.txt

#/usr/java/jdk1.5.0/java3d-utils-src.jar
#/usr/java/jdk1.5.0/java3d-demo.tar.gz
%defattr(755,root,root)

%post

%changelog
* Wed Sep 14 2004 Paul.Byrne@sun.com
- Create rpm package
