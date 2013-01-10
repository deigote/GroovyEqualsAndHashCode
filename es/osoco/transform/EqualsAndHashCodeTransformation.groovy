package es.osoco.transform

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.builder.AstBuilder
import java.lang.annotation.Annotation

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class EqualsAndHashCodeTransformation implements ASTTransformation 
{
	private final static String configurationErrorMessage =
		"Transformation ${EqualsAndHashCodeTransformation.class.name} should be applied to a class annotation"
	private final static String unexpectedErrorMessage =
		"Unexpected error when applying transformation ${EqualsAndHashCodeTransformation.class.name} to class "
	
	void visit(ASTNode[] astNodes, SourceUnit sourceUnit) 
	{	
		def (annotationNode, classNode) = astNodes?.size() >= 2 ? astNodes[0..1] : [null, null]
		try
		{
			if (annotationNode && classNode &&
				annotationNode instanceof AnnotationNode &&
				annotationNode.classNode?.name == EqualsAndHashCode.class.name &&
				classNode instanceof ClassNode)
			{
				findAstAnnotationInstance(classNode).with
				{
					annotationInstance ->
					Collection<String> includes = annotationInstance.includes()
					String logLevel = annotationInstance.logLevel()?.toLowerCase()
					addEqualsMethod(classNode, includes, logLevel)
					addHashCodeMethod(classNode, includes, logLevel)

					/*
					 def includesExpression = annotationNode.getMember('includes')
					 includes = includesExpression?.respondsTo('getExpressions') ?
						 includesExpression?.getExpressions()*.value :
						 includesExpression ? [includesExpression?.value] : null
					 */
				}
			}
			else
			{
				addError(sourceUnit, , annotationNode)
			}
		} 
		catch (t)
		{
			addError(sourceUnit, unexpectedErrorMessage + classNode?.name, annotationNode)
			t.printStackTrace();
		}
	}

	private findAstAnnotationInstance(classNode) 
	{
		Class.forName(classNode.name).annotations.find { it  instanceof EqualsAndHashCode }
	}
	
	protected void addError(sourceUnit, String msg, ASTNode expr) 
	{
		sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
			new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber()), 
			sourceUnit)
		);
	}
	
	MethodNode addEqualsMethod(ClassNode declaringClass, List<String> includes, String logLevel)
	{
		addMethod(declaringClass, 'equals', includes,
		{ 
			className, includesString ->
			"""
            boolean equals(Object obj)
			{
				obj != null && (
					(obj.respondsTo('instanceOf') && obj.instanceOf(this.getClass())) || 
					obj instanceof $className
				) &&
				(
					this.is(obj) || ${includesString}.inject(true)
					{
						isEqual, property ->
						if ("$logLevel") 
						{
							log.$logLevel "\$this - equals - comparing \$property with values \${this[property]} and \${obj[property]}"
						}
						isEqual && this[property] == obj[property]
					}
				)
			}
        	"""
        })
	}
	
	MethodNode addHashCodeMethod(ClassNode declaringClass, List<String> includes, String logLevel)
	{
		def hashCodeHelper = 'org.codehaus.groovy.util.HashCodeHelper'
		addMethod(declaringClass, 'hashCode', includes,
		{
			className, includesString ->
			"""
            int hashCode()
			{
				${includesString}.inject(${hashCodeHelper}.initHash())
				{
					currentHashCode, property ->
					if ("$logLevel") 
					{
						log.$logLevel "\$this - hashCode - appending \$property with value \${this[property]}"
					}
					${hashCodeHelper}.updateHash(currentHashCode, this[property])
				}
			}
        	"""
        })
	}
	
	private String calculateIncludesAsString(List<String> includes)
	{
		includes.collect { "'" + it + "'" }
	}
	
	private MethodNode addMethod(ClassNode declaringClass, String methodName, List<String> includes, 
		Closure methodBodyBuilder)
	{
		String className = declaringClass.name
		String includesString = calculateIncludesAsString(includes)
		logAddingMethod(methodName, className, includes)
		declaringClass.addMethod(new AstBuilder().buildFromString(
			CompilePhase.INSTRUCTION_SELECTION, false, methodBodyBuilder(className, includesString)
		).getAt(1).methods.find { it.name == methodName })
	}
	
	private logAddingMethod(methodName, className, includes)
	{
		println "Creating $methodName for class $className($includes)"
	}
	
	
}
