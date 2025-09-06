The source file samples in the subdirectories are created or updated manually
by loading a sample arrz file from folder ../arrz into Structorizer and export-
ing the contained main diagram(s) either
- via menu entry "File > Export > Code > ..." or
- from the Arranger Index via context menu entry "Export diagram/group > ..."
with the respective target language chosen.
Make sure to have export option "Involve called subroutines" enabled and option
"No conversion of the expression/instruction contents" disabled. The default
sizes for arrays / strings were configured to 50 and 128, respectively.
Ideally, the samples are updated whenever a new Structorizer version has any
substantial impact on the code generation for the target language (name diffe-
rences of generically introduced auxiliary variables are not of course regard-
ed as substantial changes).