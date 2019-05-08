object Main extends App {
  println("Hello, World!")
  val server = new CreatePoll()
  try {
    println("press enter to exit")
    System.in.read();
  }
  finally {
    server.stop()
  }
}