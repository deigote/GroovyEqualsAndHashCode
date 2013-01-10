package es.osoco.transform

@EqualsAndHashCode(includes=['a', 'b'], logLevel = 'debug')
class TestEqualsAndHashCode 
{
	private String a
	private Integer b
	private Boolean c
	
	def log = [ 
		debug: 
		{ 
			message, exception = null -> 
			println "DEBUG ${this.getClass()} - $message"
			exception?.printStackTrace()
		}
	]
	
	private TestEqualsAndHashCode() {}
	
	public static TestEqualsAndHashCode buildTest(a, b, c) {
		[a:a, b:b, c:c].inject(new TestEqualsAndHashCode()) { 
			testInstance, property -> 
			testInstance[property.key] = property.value
			testInstance
		}
	}
	
	String toString()
	{
		"Test(a=$a, b=$b, c=$c)"
	}
	
	public static void main(String[] args) 
	{
		def baseInstance = buildTest("Test 1", 5, true)
		def instancesToCompareAndExpectedResult = 
		[
			(false): 
			[
				buildTest("Test 2", 5, true),
				buildTest("Test 1", 6, true)
			],
			(true):
			[
				buildTest("Test 1", 5, true),
				buildTest("Test 1", 5, false)
			]
		]
		
		instancesToCompareAndExpectedResult.each 
		{
			shouldBeEquals, instancesToCompare ->
			instancesToCompare.each 
			{
				instanceToCompare ->
				TestEqualsAndHashCode.metaClass = null
				println "Comparing $instanceToCompare to $baseInstance"
				assert instanceToCompare.equals(baseInstance) == shouldBeEquals
				if (shouldBeEquals) assert instanceToCompare.hashCode() == baseInstance.hashCode()
			} 
		}
	}
	
}
