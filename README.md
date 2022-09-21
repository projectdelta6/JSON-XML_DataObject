# JSON-XML_DataObject

This is a Generic Data Object to handle both JSON and XML data.

I originaly created this to enable me to communicate with server APIs that used only XML, and later when the server APIs were upgraded to JSON I added JSON serializing and deserializing to it as well.

I continued to use this rather than using Gson/Moshi(etc) to deserialize into Class models because the data I was working with was dynamic and so not modelable into a class, I tried to use JsonObject and JSONObject (Java and Gson classes) to replace this DataObject but they just did not offer the same capabilities that I had build into this. 🤷‍♂️

The library is available on Jitpack:

[![](https://jitpack.io/v/projectdelta6/JSON-XML_DataObject.svg)](https://jitpack.io/#projectdelta6/JSON-XML_DataObject)
