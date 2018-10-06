

@echo off
set arg1=%1
set arg2=%2

rem java -cp "./;.ejml-v0.34-libs/*" MatrixServer.java
java -cp "./;./ejml-v0.34-libs/*" MatrixServer %arg1% %arg2%