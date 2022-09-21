package com.duck.testingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.duck.dataobject.DataObject

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		val textView: TextView = findViewById(R.id.textView)

		val test1 = DataObject()
		test1.parse(testJson)

		val test2 = DataObject()
		test2.parse(testXML)

		textView.text = """
Parsed JSON:
${test1.toFormattedJSON()}

As XML:
${test1.toFormattedXML()}

-----------------------------

Parsed XML:
${test2.toFormattedXML()}

As JSON:
${test2.toFormattedJSON()}
		"""
	}
}

val testJson: String = """
	{
	  "data": [{
	    "type": "articles",
	    "id": "1",
	    "attributes": {
	      "title": "JSON:API paints my bikeshed!",
	      "body": "The shortest article. Ever.",
	      "created": "2015-05-22T14:56:29.000Z",
	      "updated": "2015-05-22T14:56:28.000Z"
	    },
	    "relationships": {
	      "author": {
	        "data": {"id": "42", "type": "people"}
	      }
	    }
	  }],
	  "included": [
	    {
	      "type": "people",
	      "id": "42",
	      "attributes": {
	        "name": "John",
	        "age": 80,
	        "gender": "male"
	      }
	    }
	  ]
	}
""".trimIndent()

val testXML = """
	<?xml version="1.0" encoding="UTF-8"?>
	<FGA_Message>
		<Device_descriptor
			instrument="FGA"
			manufacturer="Telegan"
			model="SprintV3"
			software_version="3.2"
			software_issue="1.04"
			serial_number="V300ANE0361"
			calibration_due="15/02/2015"
			owner_line1="Your Company Name"
			owner_line2="Your Company Phone"/>
		<Flue_log_i200
			time="15:00"
			date="11/05/2014"
			log_id="12"
			fuel_type="Natural gas">
			<V3_Readings
				O2="20.9"
				CO="0"
				CO2="####.#"
				CO_CO2_ratio="#.####"
				pressure="0.00"
				temperature_net="#####"
				temperature_flue="#####"
				efficiency="####.#"
				efficiency_units="Net"
				excess_air="####.#"/>
		</Flue_log_i200>
	</FGA_Message>
""".trimIndent()