package multichat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultichatServer {
   
   ExecutorService executorService;
   ServerSocketChannel serverSocketChannel;
   // 클라이언트 접속 정보를 저장하는 컬렉션  (key- 대화명 , value-SocketChannel)
   Hashtable<String, SocketChannel> clinets;
   
   public MultichatServer() {
      clinets = new Hashtable<String, SocketChannel>();
      executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
   }
   
   void startServer() {
      try {
         serverSocketChannel = ServerSocketChannel.open();
         serverSocketChannel.configureBlocking(true);
         serverSocketChannel.bind(new InetSocketAddress("localhost",7777));
      } catch(Exception e) {
         if(serverSocketChannel.isOpen()) { stopServer(); }
         return;
      }
      
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            System.out.println("[서버 시작]");
            while(true) {
               try {
                  SocketChannel socketChannel = serverSocketChannel.accept();
                  System.out.println("[연결 수락:" + socketChannel.getRemoteAddress() + ":" + Thread.currentThread().getName() + "]");
                  
                  Client client = new Client(socketChannel);
                  client.receive();
                  
               } catch (Exception e) {
                  if(serverSocketChannel.isOpen()) { stopServer(); }
                  break;
                        
               }
               
            }
         }
   };
   
   executorService.submit(runnable);
   }
   
   void stopServer() {
      try {
         Iterator<String> iterator = clinets.keySet().iterator();
         while(iterator.hasNext()) {
            SocketChannel client = clinets.get(iterator.next());
            client.close();
            iterator.remove();
         }
         if(serverSocketChannel!=null && serverSocketChannel.isOpen()) {
                  serverSocketChannel.close();
         }
         if(executorService!=null && !executorService.isShutdown()) {
            executorService.shutdown();
         }
         System.out.println("[서버 멈춤]");
         System.exit(0);
      } catch (Exception e) {}
   }
   
   class Client {
      SocketChannel socketChannel;
      
      Client(SocketChannel socketChannel) {
         this.socketChannel = socketChannel;
      }
      
      void receive() {
         Runnable runnable = new Runnable() {
            @Override
            public void run() {
               ByteBuffer byteBuffer = ByteBuffer.allocate(100);
               Charset charset = Charset.forName("UTF-8");
               int readByteCount = -1;
               String msg = "";
               String name = "";
               
               try {
                  readByteCount = socketChannel.read(byteBuffer); // 처음에 는 대화명
                  byteBuffer.flip();
                  name = charset.decode(byteBuffer).toString();
                  msg = "#" + name + "님이 들어오셨습니다.";
                  sendToAll(msg);
                  System.out.println(msg);
                  clinets.put(name, socketChannel);
                  System.out.println("현재 서버 접속자수는 " + clinets.size() +" 입니다");
               
               while(socketChannel != null) {
                  byteBuffer.clear();
                  readByteCount = socketChannel.read(byteBuffer);
                  //클라이언트가 정상적으로 SocketChannel의 close() 호출 했을 경우 
                  
                  if(readByteCount == -1) {
                     break;
                  }
                  byteBuffer.flip();
                  msg = charset.decode(byteBuffer).toString();
                  sendToAll(msg);
                  System.out.println(msg);
               }
               
            } catch(Exception e) {      
               } finally {
                  try {
                     clinets.remove(name);
                     msg = "#" + name + "님이 나가셨습니다.";
                     sendToAll(msg);
                     System.out.println(msg);
                     
                     socketChannel.close();
                     System.out.println("현재 서버 접속자수는 " +clinets.size()+ " 입니다 ");
                  } catch (IOException e2) {}
               }
            }
      };
      executorService.submit(runnable);
      }
      
      void sendToAll(String msg) {
         Runnable runnable = new Runnable() {
            @Override
            public void run() {
               Iterator<String> it = clinets.keySet().iterator();
               while(it.hasNext()) {
                  try {
                     SocketChannel socketChannel = clinets.get(it.next());
                     Charset charset = Charset.forName("UTF-8");
                     ByteBuffer byteBuffer = charset.encode(msg);
                     socketChannel.write(byteBuffer);
                  } catch(Exception e) {
                     
                  }
               }
            }
         };
         executorService.submit(runnable);
      }
   }
   public static void main(String[] args) {
      // TODO Auto-generated method stub
      new MultichatServer().startServer();
   }
}