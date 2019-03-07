Structorizer / Turtleizer
The interfaces DiagramController and DelayableDiagramController placed here came from
lu.fisch.structorizer.executor but had to be moved to an independent package where e.g.
TurtleBox may refer to it without requiring Structorizer packages.
This was essential to set enhancement #441 into work.
Kay Gürtzig 2017-10-27

DiagramController is intended to serve as base for façade classes of modules or devices
that can be controlled by Structorizer diagrams. It defines a very generic API now,
allowing to expose nearly any kind of method as Structorizer function or procedure,
provided the data types can be addressed from diagram code.
Kay Gürtzig 2017-10-29

DiagramController API was enhanced with two methods isFocussed() and requestFocus()
the dummy implementations for which do not do anything but may be overridden to allow
elementary window focus control as requested by issue #366.
Kay Gürtzig 2019-03-02