val t = osc.UDP.Transmitter("127.0.0.1" -> 57121)
t.dump()
t.connect()
t ! osc.Message("/shutdown")
