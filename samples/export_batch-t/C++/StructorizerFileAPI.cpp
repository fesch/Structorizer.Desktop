/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    File API implementation for the CPlusPlusGenerator.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2016-12-24      First Issue
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      - The File API of Structorizer is implemented as static methods on this non-instantiable class
 *
 ******************************************************************************************************///

#include "StructorizerFileAPI.h"
#include <sstream>

StructorizerFileAPI::FileMap StructorizerFileAPI::fileMap;

StructorizerFileAPI::StructorizerFileAPI()
{
}

StructorizerFileAPI::~StructorizerFileAPI()
{
}

int StructorizerFileAPI::fileOpen(const std::string& filePath)
{
	return fileMap.addFile(filePath, std::ios_base::in);
}

int StructorizerFileAPI::fileCreate(const std::string& filePath)
{
	return fileMap.addFile(filePath, std::ios_base::out);
}

int StructorizerFileAPI::fileAppend(const std::string& filePath)
{
	return fileMap.addFile(filePath, std::ios_base::out);
}

void StructorizerFileAPI::fileClose(int fileNo)
{
	fileMap.removeFile(fileNo);
}

bool StructorizerFileAPI::fileEOF(int fileNo)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::in);
	bool isEOF = file.eof();
	if (!isEOF && file.peek() == EOF) {
		isEOF = true;
	}
	return isEOF;
}

StructorizerFileAPI::FileAPI_value StructorizerFileAPI::fileRead(int fileNo)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::in);
	std::string word = "";
	if (file.peek() == EOF) {
		return FileAPI_value();
	}
	file >> word;
	FileAPI_value value(word);
	while (!value.isComplete() && (file >> word)) {
		value.append(word);
	}
	return value;
}

char StructorizerFileAPI::fileReadChar(int fileNo)
{
	char ch = '\0';
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::in);
	file.get(ch);
	return ch;
}

int StructorizerFileAPI::fileReadInt(int fileNo)
{
	int value = 0;
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::in);
	if (!(file >> value)) {
		throw new FileAPI_exception("fileReadInt: No integer value readable from file!");
	}
	return value;
}

double StructorizerFileAPI::fileReadDouble(int fileNo)
{
	double value = 0.0;
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::in);
	if (!(file >> value)) {
		throw new FileAPI_exception("fileReadDouble: No floating-point value readable from file!");
	}
	return value;
}

std::string StructorizerFileAPI::fileReadLine(int fileNo)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::in);
	std::string line;
	if (!std::getline(file, line)) {
		throw new FileAPI_exception("fileReadLine: Attempt to read data past end of file!");
	}
	return line;
}

void StructorizerFileAPI::fileWrite(int fileNo, int data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data;
}
void StructorizerFileAPI::fileWrite(int fileNo, double data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data;
}
void StructorizerFileAPI::fileWrite(int fileNo, std::string data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data;
}
void StructorizerFileAPI::fileWrite(int fileNo, char data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data;
}
void StructorizerFileAPI::fileWrite(int fileNo, bool data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data;
}

void StructorizerFileAPI::fileWriteLine(int fileNo, int data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data << std::endl;
}
void StructorizerFileAPI::fileWriteLine(int fileNo, double data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data << std::endl;
}
void StructorizerFileAPI::fileWriteLine(int fileNo, std::string data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data << std::endl;
}
void StructorizerFileAPI::fileWriteLine(int fileNo, char data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data << std::endl;
}
void StructorizerFileAPI::fileWriteLine(int fileNo, bool data)
{
	std::fstream& file = fileMap.getFile(fileNo, std::ios_base::out);
	file << data << std::endl;
}

StructorizerFileAPI::FileMapEntry::FileMapEntry(int fileNo, const std::string& filePath, std::ios_base::openmode mode)
: fileNo(fileNo)
, strm(filePath, (mode == std::ios_base::app) ? mode | std::ios_base::out : mode)
, forOutput(mode != std::ios_base::in)
{
	if (!this->strm.is_open()) {
		throw new FileAPI_exception("File API: File opening failed!");
	}
}

StructorizerFileAPI::FileMap::FileMap()
: nFiles(0)
{
}

int StructorizerFileAPI::FileMap::addFile(const std::string& filePath, std::ios_base::openmode mode)
{
	try {
		this->entries.push_back(new FileMapEntry(this->nFiles + 1, filePath, mode));
		return ++(this->nFiles);
	}
	catch (FileAPI_exception ex) {}
	return -errno;
}

bool StructorizerFileAPI::FileMap::removeFile(int fileNo)
{
	bool done = false;
	std::vector<FileMapEntry*>::const_iterator iter = this->getIterOf(fileNo);
	if (iter != this->entries.end()) {
		delete (*iter);	// This is supposed automatically to close the stream as well
		this->entries.erase(iter);
		done = true;
	}
	return done;
}

std::fstream& StructorizerFileAPI::FileMap::getFile(int fileNo, std::ios_base::openmode mode) const
{
	std::vector<FileMapEntry*>::const_iterator iter = this->getIterOf(fileNo);
	if (iter == this->entries.end()
		|| (mode != 0) && ((mode == std::ios_base::out) != (*iter)->forOutput)) {
		throw new FileAPI_exception((mode == std::ios_base::out) ? "Invalid file number or file not open for writing." : "Invalid file number or file not open for reading.");
	}
	return (*iter)->strm;
}


std::vector<StructorizerFileAPI::FileMapEntry*>::const_iterator StructorizerFileAPI::FileMap::getIterOf(int fileNo) const
{
	std::vector<StructorizerFileAPI::FileMapEntry*>::const_iterator iter = this->entries.begin();
	while (iter != this->entries.end() && fileNo > (*iter)->fileNo) {
		++iter;
	}
	if (iter != this->entries.end() && fileNo != (*iter)->fileNo) {
		iter = this->entries.end();
	}
	return iter;
}

StructorizerFileAPI::FileAPI_value::FileAPI_value()
: type(FAV_VOID)
, valStr("")
, valInt(0)
, valDbl(0)
{
}
StructorizerFileAPI::FileAPI_value::FileAPI_value(std::string value)
: type(FAV_STR)
, valStr(value)
, valInt(0)
, valDbl(0)
{
	std::istringstream istr(this->valStr);
	if (istr >> this->valDbl && (istr.peek() == EOF)) {
		this->type = FAV_DBL;
		istr.seekg(0, istr.beg);
		if (istr >> this->valInt && (istr.peek() == EOF)) {
			this->type = FAV_INT;
		}
	}
}

StructorizerFileAPI::FileAPI_value::operator int() const
{
	// TODO throw an exception if type != VAL_INT?
	return this->valInt;
}
StructorizerFileAPI::FileAPI_value::operator double() const
{
	// TODO throw an exception if type != VAL_DBL?
	return this->valDbl;
}
StructorizerFileAPI::FileAPI_value::operator const std::string() const
{
	if (this->type == FAV_VOID) { throw new FileAPI_exception("Attempt to read data past end of file!"); }
	std::string str(this->valStr);
	if (!str.length() > 1 && 
		(str.front() == '"' && str.back() == '"' ||
		str.front() == '\'' && str.back() == '\'')) {
		str = str.substr(1, str.length() - 2);
	}
	return str;
}
StructorizerFileAPI::FileAPI_value::operator bool() const
{
	return this->type != FAV_VOID;
}
bool StructorizerFileAPI::FileAPI_value::isInt() const
{
	return this->type == FAV_INT;
}
bool StructorizerFileAPI::FileAPI_value::isDouble() const
{
	return this->type == FAV_DBL;
}
bool StructorizerFileAPI::FileAPI_value::isString() const
{
	return this->type == FAV_STR;
}
bool StructorizerFileAPI::FileAPI_value::append(std::string next)
{
	this->valStr += " " + next;
	return this->isComplete();
}
bool StructorizerFileAPI::FileAPI_value::isComplete() const
{
	bool isCompl = true;
	if (this->type == FAV_STR && !this->valStr.empty()) {
		const char leftDelimiters[] = { '"', '\'', '{' };
		const char rightDelimiters[] = { '"', '\'', '}' };
		for (int i = 0; isCompl && i < sizeof(leftDelimiters) / sizeof(char); i++) {
			isCompl = this->valStr.front() != leftDelimiters[i] || this->valStr.back() == rightDelimiters[i];
		}
	}
	return isCompl;
}
