object WorkerApp extends App{
  println("Worker App started")

  val workerRef = new SomeClass("Worker")
  workerRef.SayWhatYouHave

  println("Worker App ended")
}
