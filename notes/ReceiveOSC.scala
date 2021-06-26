val c = osc.UDP.Config()
c.localIsLoopback = true
c.localPort = 57120
val r = osc.UDP.Receiver(c)
r.dump()
r.connect()

