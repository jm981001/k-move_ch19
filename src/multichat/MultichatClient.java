package multichat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class MultichatClient {
   SocketChannel socketChannel;
   void startClient(String name) {
      Thread thread = new Thread() {
         @Override
         public void run() {
            try {
               socketChannel = SocketChannel.open();
               socketChannel.configureBlocking(true);
               socketChannel.connect(new InetSocketAddress("localhost", 7777));
               System.out.println("[연결완료: " + socketChannel.getRemoteAddress() + "]");
               
               ClientSender sender = new ClientSender(socketChannel, name);
               ClientReceiver receiver = new ClientReceiver(socketChannel);
               sender.start();
               receiver.start();
            } catch(Exception e) {
               System.out.println("[서버통신 안됨]");
               if(socketChannel.isOpen()) {
                  stopClient();
                  }
                  return;
               }
            }
         };
         thread.start();
         
      }
      
      void stopClient() {
         try {
            System.out.println("[연결 끊음]");
            if(socketChannel!=null && socketChannel.isOpen()) {
               socketChannel.close();
            }
            System.exit(0);
            
         } catch (IOException e) {
            
         }
      }
      
      public static void main(String[] args) {
         if(args.length != 1) {
            System.out.println("실행시 채팅 대화명이 필요합니다.");
            System.exit(0);
         }
         
         new MultichatClient().startClient(args[0]);
      }
      
      //클라이언트의 메시지를 보내는 스레드
      static class ClientSender extends Thread {
         SocketChannel socketChannel = null;
         String name;
         
         public ClientSender(SocketChannel socketChannel,String name) {
            this.socketChannel = socketChannel;
            this.name = name;
         }
         
         public void run() {
            Scanner sc = new Scanner(System.in);
            String msg;
            
            try {
               Charset charset = Charset.forName("UTF-8");
               ByteBuffer byteBuffer = charset.encode(name);
               socketChannel.write(byteBuffer); //제일 처음 대화명 보내기
               
               while(!(msg=sc.nextLine()).equals("exit")) { //콘솔 키보드를 통해 입력받은
                  msg = "[" + name + "]" + msg; //채팅 메시지 보내기
                  
                  byteBuffer = charset.encode(msg);
                  socketChannel.write(byteBuffer);
               }
               new MultichatClient().stopClient();
               //System.exit(0); //exit를 입력한 경우 클라이언트 프로그램 종료
            } catch (Exception e) {
               System.out.println("[서버 통신 안됨]");
               new MultichatClient().stopClient();
            }
         }
      }
      
      //다른 클라이언트들의 메시지를 받는 스레드
      static class ClientReceiver extends Thread {
         SocketChannel socketChannel = null;
         
         public ClientReceiver(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
         }
         
         public void run() {
            while(socketChannel != null) {
               try {
                  ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                  
                  //서버가 비정상적으로 종료했을 경우 IOException 발생
                  int readByteCount = socketChannel.read(byteBuffer);
                  
                  //서버가 정상적으로 Socket의 close()를 호출했을 경우
                  if(readByteCount == -1) {
                     throw new IOException();
                  }
                  byteBuffer.flip();
                  Charset charset = Charset.forName("UTF-8");
                  String data = charset.decode(byteBuffer).toString();
                  
                  System.out.println(data);
               } catch (Exception e) {
                  System.out.println("[서버 통신 안됨]");
                  new MultichatClient().stopClient();
                  break;
               }
            }
         }
   }
}
/*
C:\Users\SONJUNGMIN>ipconfig

Windows IP 구성


무선 LAN 어댑터 로컬 영역 연결* 3:

   미디어 상태 . . . . . . . . : 미디어 연결 끊김
   연결별 DNS 접미사. . . . :

무선 LAN 어댑터 로컬 영역 연결* 12:

   미디어 상태 . . . . . . . . : 미디어 연결 끊김
   연결별 DNS 접미사. . . . :

무선 LAN 어댑터 Wi-Fi:

   연결별 DNS 접미사. . . . :
   링크-로컬 IPv6 주소 . . . . : fe80::850d:ba43:bde4:67f4%19
   IPv4 주소 . . . . . . . . . : 192.168.123.187
   서브넷 마스크 . . . . . . . : 255.255.255.0
   기본 게이트웨이 . . . . . . : 192.168.123.254

이더넷 어댑터 Bluetooth 네트워크 연결:

   미디어 상태 . . . . . . . . : 미디어 연결 끊김
   연결별 DNS 접미사. . . . :

C:\Users\SONJUNGMIN>cd..

C:\Users>cd..

C:\>cd
C:\

C:\>dir
 C 드라이브의 볼륨에는 이름이 없습니다.
 볼륨 일련 번호: 7896-2E0A

 C:\ 디렉터리

2021-08-17  오후 11:16    <DIR>          .metadata
2021-08-30  오후 03:01    <DIR>          ADE
2020-09-04  오전 12:55                87 agentlog.txt
2021-10-28  오후 04:37    <DIR>          apache-tomcat-10.0.12
2021-08-30  오후 04:28    <DIR>          app
2021-11-16  오후 04:58    <DIR>          K-MOVE IT
2019-12-07  오후 06:14    <DIR>          PerfLogs
2021-10-24  오후 07:03    <DIR>          Program Files
2021-05-22  오후 10:50    <DIR>          Program Files (x86)
2021-11-12  오후 03:32    <DIR>          Temp
2021-10-28  오후 01:54           335,592 UkLog.dat
2021-08-18  오후 02:40    <DIR>          Users
2021-11-17  오후 01:55    <DIR>          Windows
               2개 파일             335,679 바이트
              11개 디렉터리  110,680,612,864 바이트 남음

C:\>cd K-MOVE IT

C:\K-MOVE IT>dir
 C 드라이브의 볼륨에는 이름이 없습니다.
 볼륨 일련 번호: 7896-2E0A

 C:\K-MOVE IT 디렉터리

2021-11-16  오후 04:58    <DIR>          .
2021-11-16  오후 04:58    <DIR>          ..
2021-08-18  오후 03:16    <DIR>          .metadata
2021-08-20  오후 05:50            23,564 0820손정민.zip
2021-11-09  오후 01:59    <DIR>          ch015
2021-08-18  오후 05:55    <DIR>          ch03
2021-08-19  오후 03:16    <DIR>          ch04
2021-08-24  오후 02:27    <DIR>          ch05
2021-08-25  오후 04:39    <DIR>          ch06
2021-08-27  오후 02:46    <DIR>          ch07
2021-10-25  오전 12:34    <DIR>          ch08
2021-10-25  오전 12:54    <DIR>          ch09
2021-10-28  오전 12:31    <DIR>          ch10
2021-11-02  오후 02:23    <DIR>          ch11
2021-11-01  오후 02:07    <DIR>          ch12
2021-11-03  오후 02:18    <DIR>          ch13
2021-11-03  오후 05:30    <DIR>          ch14
2021-11-05  오후 04:50    <DIR>          ch15
2021-11-09  오후 04:32    <DIR>          ch16
2021-11-12  오전 09:36    <DIR>          ch18
2021-11-16  오후 04:58    <DIR>          ch19
2021-08-18  오후 05:57    <DIR>          exercise
2021-08-27  오후 05:38    <DIR>          gisa
2021-10-19  오전 01:18               211 Member.java
2021-10-19  오전 01:18                54 MemberService.java
2021-10-19  오전 01:18               147 MemberServiceTest.java
2021-10-19  오전 01:18               166 MemberTest.java
2021-10-19  오전 01:18                48 Printer.java
2021-10-19  오전 01:18               281 PrinterExample.java
2021-10-29  오후 02:27    <DIR>          Servers
2021-10-19  오전 01:18                52 ShopService (1).java
2021-10-19  오전 01:16                52 ShopService.java
2021-10-19  오전 01:18               392 ShopServiceTest.java
2021-09-03  오후 03:07    <DIR>          sql
2021-10-29  오후 02:26    <DIR>          StudyJSP
              10개 파일              24,967 바이트
              25개 디렉터리  110,680,481,792 바이트 남음

C:\K-MOVE IT>cd ch19

C:\K-MOVE IT\ch19>dir
 C 드라이브의 볼륨에는 이름이 없습니다.
 볼륨 일련 번호: 7896-2E0A

 C:\K-MOVE IT\ch19 디렉터리

2021-11-16  오후 04:58    <DIR>          .
2021-11-16  오후 04:58    <DIR>          ..
2021-11-16  오후 04:58               396 .classpath
2021-11-16  오후 04:58               380 .project
2021-11-16  오후 04:58    <DIR>          .settings
2021-11-17  오후 02:05    <DIR>          bin
2021-11-17  오후 02:05    <DIR>          src
               2개 파일                 776 바이트
               5개 디렉터리  110,680,481,792 바이트 남음

C:\K-MOVE IT\ch19>cd bin

C:\K-MOVE IT\ch19\bin>dir
 C 드라이브의 볼륨에는 이름이 없습니다.
 볼륨 일련 번호: 7896-2E0A

 C:\K-MOVE IT\ch19\bin 디렉터리

2021-11-17  오후 02:05    <DIR>          .
2021-11-17  오후 02:05    <DIR>          ..
2021-11-17  오후 02:05    <DIR>          multichat
               0개 파일                   0 바이트
               3개 디렉터리  110,680,461,312 바이트 남음

C:\K-MOVE IT\ch19\bin>java multichat.MultichatServer
[서버 시작]
[연결 수락:/127.0.0.1:62929:pool-1-thread-1]
#손정민님이 들어오셨습니다.
현재 서버 접속자수는 1 입니다
[손정민]
[손정민]ff*/