Structorizer / Turtleizer
The interfaces DiagramControl placed here came from lu.fisch.structorizer.execute
but had to be moved to an independent package where e.g. TurtleBox may refer
to it without requiring Structorizer packages.
This was essential to set enhancement #441 into work.
Kay Gürtzig 2017-10-27

DiagramControl is intended to serve as base for facade classes of modules or devices
that can be controlled by Structorizer diagrams. It defines a very generic API now,
allowing to expose nearly any kind of method as Structorizer function or procedure,
provided the data types can be addressed from diagram code.
Kay Gürtzig 2017-10-29