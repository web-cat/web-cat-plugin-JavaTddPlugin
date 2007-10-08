/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2007 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat;

import java.lang.reflect.*;
import static junit.framework.Assert.*;

//-------------------------------------------------------------------------
/**
 *  This class provides static helper methods to use reflection to check
 *  for and invoke methods on objects.  It is intended for use in test
 *  cases, and makes it easier to write test cases that compile successfully
 *  but fail at run-time if the class under test fails to provide
 *  required methods (or provides them with the wrong signature).  For
 *  Web-CAT users, this makes it possible to get partial scores, even when
 *  some methods are not even provided in the class under test.
 *  <p>
 *  Consider a situation where you are writing a test case that invokes the
 *  <code>doIt()</code> method on a given object.  You might do it like
 *  this:
 *  </p>
 *  <pre>
 *  MyType result = receiver.doIt(param1, param2);  // returns a value
 *  // ...
 *  receiver.doSomethingElse();  // a void method with no parameters
 *  </pre>
 *  <p>The first line passes two parameters to <code>doIt()</code> and stores
 *  the return value in a local variable called <code>result</code>.  All
 *  this would be fine if the class under test actually provides a method
 *  called <code>doIt()</code> with the appropriate signature and return
 *  type.  Otherwise, you would get a compile-time error.  But on Web-CAT
 *  a compilation error in a test suite would give a resulting score of
 *  zero--no successful compilation, no partial credit.  The story is similar
 *  with the second line, which simply calls a void method on the class
 *  under test.
 *  </p>
 *  <p>What if, instead, you wanted to write test cases that <em>checked
 *  for</em> specific methods, and either succeeded or failed depending on
 *  whether the method was present?  You can use the methods in this
 *  class to write such test cases.  So instead of writing the call
 *  above, you could do this:</p>
 *  <pre>
 *  // At the top of your test class
 *  import static net.sf.webcat.ReflectionSupport.*;
 *
 *  // ...
 *
 *  // For a method that returns a result:
 *  MyType result = invoke(receiver, MyType.class, "doIt", param1, param2);
 *
 *  // Or for a void method:
 *  invoke(receiver, "doSomethingElse");
 *  </pre>
 *  <p>
 *  The syntax is simple and straight forward.  The overloaded invoke
 *  method can be used on functions (methods that return a value) or
 *  procedures (void methods that return nothing).  For methods that
 *  return a value, you specify the type of the return value as the
 *  second parameter.  If you omit the return type, then it is assumed to
 *  be "void".  You specify the name of the method as a string, and then
 *  a variable length set of arguments.
 *  </p>
 *  <p>
 *  If you are calling a method that is returning a primitive type, be
 *  sure to use the corresponding wrapper class as the expected return
 *  value:
 *  </p>
 *  <pre>
 *  // Instead of boolean answer = receiver.equals(anotherObject);
 *  Boolean answer = invoke(receiver, Boolean.class, "equals", anotherObject);

 *  // Or in an assert, using auto-unboxing:
 *  assertTrue(invoke(receiver, Boolean.class, "equals", anotherObject));
 *  </pre>
 *  <p>
 *  Any errors that occur during reflection, such as failing to find the
 *  required method, failing to find a method with the required signature,
 *  finding a method that is not public, etc., are converted into
 *  appropriate test case errors with meaningful diagnostic hints for the
 *  student.  Any exceptions are wrapped in a RuntimeException and need not
 *  be explicitly caught by the caller (they will turn into test case
 *  failures as well).
 *  </p>
 *
 *  @author  stedwar2
 *  @version $Id$
 */
public class ReflectionSupport
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * This class only provides static methods, to the constructor is
     * private.
     */
    private ReflectionSupport()
    {
        // Nothing to do
    }

    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Returns the name of the given class without any package prefix.
     * If the argument is an array type, square brackets are added to
     * the name as appropriate.  This method isuseful in generating
     * diagnostic messages or feedback.
     * @param aClass The class to generate a name for
     * @return The class' name, without the package part, e.g., "String"
     *     instead of "java.lang.String"
     */
    public static String simpleClassName(Class aClass)
    {
        if (aClass == null) return "null";
        String result = aClass.getName();

        // If it is an array, add appropriate number of brackets
        try
        {
            Class cl = aClass;
            while (cl.isArray())
            {
                result += "[]";
                cl = cl.getComponentType();
            }
        }
        catch (Throwable e)
        {
            // Swallow it and stick with the bare class name
        }

        int pos = result.lastIndexOf('.');
        if (pos >= 0)
        {
            result = result.substring(pos + 1);
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Constructs a printable version of a method's name, given
     * the method name and its parameter type(s), if any.
     * Useful in generating diagnostic messages or feedback.
     * @param name   The method name
     * @param params The method's parameter type(s), in order
     * @return A printable version of the method name, like
     *     "myMethod()" or "yourMethod(String, int)"
     */
    public static String simpleMethodName(String name, Class ... params)
    {
        return name + simpleArgumentList(params);
    }


    // ----------------------------------------------------------
    /**
     * Constructs a printable version of a method's argument list, including
     * the parentheses, given the method's parameter type(s), if any.
     * @param params The method's parameter type(s), in order
     * @return A printable version of the argument list built using
     *     {@link #simpleClassName(Class)}, like "(String, int)"
     */
    public static String simpleArgumentList(Class ... params)
    {
        String result = "(";
        boolean needsComma = false;
        for (Class c : params)
        {
            if (needsComma)
            {
                result += ", ";
            }
            if (c == null)
            {
                result += "null";
            }
            else
            {
                result += simpleClassName(c);
            }
            needsComma = true;
        }
        result += ")";
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Constructs a printable version of a method's name.  Unlike
     * {@link Method#toString()}, this one uses {@link #simpleClassName(Class)}
     * so package info is eliminated from the types in the resulting
     * string.  It also omits exception information, unlike Method.toString().
     * @param method The method to print
     * @return A printable version of the method name, like
     *     "public void MyClass.myMethod()" or
     *     "public String YourClass.yourMethod(String, int)"
     */
    public static String simpleMethodName(Method method)
    {
        StringBuffer sb = new StringBuffer();
        int mod = method.getModifiers();
        if (mod != 0)
        {
            sb.append(Modifier.toString(mod) + " ");
        }
        sb.append(simpleClassName(method.getReturnType()) + " ");
        sb.append(simpleClassName(method.getDeclaringClass()) + ".");
        sb.append(method.getName());
        sb.append(simpleArgumentList(method.getParameterTypes()));
        return sb.toString();
    }


    // ----------------------------------------------------------
    /**
     * Determine whether an actual argument type matches a formal argument
     * type.  This uses {@link Class#isAssignableFrom(Class)}, but gives
     * the correct results for primitive types vs. wrapper types.
     * @param actual The type of the actual parameter
     * @param formal The type of the formal parameter
     * @return True if the actual value can be passed into a parameter
     *    declared using the formal type
     */
    public static boolean actualMatchesFormal(Class<?> actual, Class<?> formal)
    {
        boolean result = formal.isAssignableFrom(actual);
        if (!result)
        {
            result =
                ( (    formal.equals(byte.class)
                    || formal.equals(short.class)
                    || formal.equals(int.class)
                    || formal.equals(long.class)
                    || formal.equals(float.class)
                    || formal.equals(double.class) )
                  && Number.class.isAssignableFrom(actual) )
                || ( formal.equals(boolean.class)
                     && actual.equals(Boolean.class) )
                || ( formal.equals(char.class)
                     && actual.equals(Character.class) );
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Look up a method by name and parameter profile, turning any
     * errors into test case failures with appropriate hint messages.
     * Only looks up methods that are declared in the specified class,
     * not inherited methods.  Assumes the intended method should be
     * public, and fails with an appropriate hint if it is not.
     * @param c The type of the receiver
     * @param name The method name
     * @param params The method's parameter profile
     * @return The corresponding Method object
     */
    public static Method getMethod(
        Class<?> c, String name, Class ... params)
    {
        Method m = null;
        try
        {
            m = c.getDeclaredMethod(name, params);
        }
        catch (NoSuchMethodException e)
        {
            String message = c + " is missing method "
                + simpleMethodName(name, params);
            fail(message);
        }
        catch (SecurityException e)
        {
            String message = "method " + simpleMethodName(name, params)
                + " in " + c + " cannot be accessed (should be public)";
            fail(message);
        }
        if (m != null && !Modifier.isPublic(m.getModifiers()))
        {
            fail(simpleMethodName(m) + " should be public");
        }
        return m;
    }


    // ----------------------------------------------------------
    /**
     * Look up a method by name and parameter profile, finding the
     * method that will accept the given list of parameters (not requiring
     * an exact match on parameter types).  It turns any
     * errors into test case failures with appropriate hint messages.
     * Only looks up methods that are declared in the specified class,
     * not inherited methods.  Assumes the intended method should be
     * public, and fails with an appropriate hint if it is not.
     * Note that this method <b>does not handle variable argument lists</b>
     * in the target method for which it is searching.
     * @param c The type of the receiver
     * @param name The method name
     * @param params The method's parameter profile
     * @return The corresponding Method object
     */
    public static Method getMatchingMethod(
        Class<?> c, String name, Class ... params)
    {
        Method result = null;
        Method methodWithSameName = null;
        Method methodWithSameParamCount = null;
        if (params == null) { params = new Class[0]; }
        for (Method m : c.getMethods())
        {
            if (m.getName().equals(name))
            {
                methodWithSameName = m;
                Class<?>[] paramTypes = m.getParameterTypes();
                if (params.length == paramTypes.length)
                {
                    methodWithSameParamCount = m;
                    result = m; // maybe ... we'll clear it if wrong
                    for (int i = 0; i < params.length; i++)
                    {
                        if (params[i] != null)
                        {
                            // If the actual is non-null, check to see if
                            // it can be assigned to the formal correctly.
                            if (actualMatchesFormal(params[i], paramTypes[i]))
                            {
                                result = null;
                                break;
                            }
                        }
                        else if (paramTypes[i].isPrimitive())
                        {
                            // If actual is null, then the formal can't
                            // be a primitive
                            result = null;
                            break;
                        }
                    }
                    if (result != null)
                    {
                        // If we found a match that can accept all the
                        // parameters  ...
                        break;
                    }
                }
            }
        }
        if (result == null)
        {
            String message = null;
            if (methodWithSameParamCount != null)
            {
                message = simpleMethodName(methodWithSameParamCount)
                    + " cannot be called with argument"
                    + ((params.length == 1) ? "" : "s")
                    + " of type "
                    + simpleArgumentList(params)
                    + ": incorrect parameter type(s)";
            }
            else if (methodWithSameName != null)
            {
                message = simpleMethodName(methodWithSameName);
                if (params.length == 0)
                {
                    message += " cannot be called with no arguments";
                }
                else
                {
                    message += " cannot be called with argument"
                        + ((params.length == 1) ? "" : "s")
                        + " of type "
                        + simpleArgumentList(params);
                }
                message += ": incorrect number of parameters";
            }
            else
            {
                message = "" + c + " is missing public method "
                    + simpleMethodName(name, params);
            }
            fail(message);
        }
        else if (!Modifier.isPublic(result.getModifiers()))
        {
            fail(simpleMethodName(result) + " should be public");
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Dynamically look up and invoke a method on a target object, with
     * appropriate hints if any failures happen along the way.
     * @param receiver The object to invoke the method on
     * @param returnType The expected type of the method's return value.
     *     Use null (or <code>void.class</code>) if the method that is
     *     looked up is a void method.
     * @param methodName The name of the method to invoke
     * @param params The parameters to pass to the method
     * @param <T> The generic parameter T is deduced from the returnType
     * @return The results from invoking the given method
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(
        Object receiver,
        Class<T> returnType,
        String methodName,
        Object ... params)
    {
        Object result = null;
        Class targetClass = receiver.getClass();
        Class[] paramProfile = null;
        if (params != null)
        {
            paramProfile = new Class[params.length];
            for (int i = 0; i < params.length; i++)
            {
                if ( params[i] == null)
                {
                    // A null indicates we'll try to pass null as an
                    // actual in the getMatchingMethod() search
                    paramProfile[i] = null;
                }
                else
                {
                    paramProfile[i] = params[i].getClass();
                }
            }
        }
        Method m = getMatchingMethod(targetClass, methodName, paramProfile);

        result = invoke(receiver, m, params);

        if (result != null)
        {
            if (returnType != null)
            {
                assertTrue("method" + simpleMethodName(m)
                    + " should be a void method",
                    returnType != void.class);
                assertTrue("method " + simpleMethodName(m)
                    + " did not produce result of type "
                    + simpleClassName(returnType),
                    returnType.isAssignableFrom(result.getClass()));
            }
            else
            {
                fail("method " + simpleMethodName(m)
                    + " should be a void method");
            }
        }
        // The cast below is technically unsafe, according to the compiler,
        // but will never be violated, due to the assertion above.
        return (T)result;
    }


    // ----------------------------------------------------------
    /**
     * Dynamically look up and invoke a method on a target object, with
     * appropriate hints if any failures happen along the way.  This
     * version is intended for calling "void" methods that have no
     * return value.
     * @param receiver The object to invoke the method on
     * @param methodName The name of the method to invoke
     * @param params The parameters to pass to the method
     */
    public static void invoke(
        Object receiver,
        String methodName,
        Object ... params)
    {
        invoke(receiver, void.class, methodName, params);
    }


    // ----------------------------------------------------------
    /**
     * Just like {@link Method#invoke(Object, Object...)}, but converts
     * any thrown exceptions into RuntimeExceptions.
     * @param receiver The object to invoke the method on
     * @param method The method to invoke
     * @param params The parameters to pass to the method
     * @return The result from the method
     */
    public static Object invoke(
        Object receiver, Method method, Object ... params)
    {
        Object result = null;
        try
        {
            result = method.invoke(receiver, params);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e;
            while (cause.getCause() != null)
            {
                cause = cause.getCause();
            }
            throw new RuntimeException(cause);
        }
        catch (IllegalAccessException e)
        {
            // This should never happen, since getMethod() has already
            // done the appropriate checks.
            throw new RuntimeException(e);
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Look up a constructor by parameter profile, finding the
     * constructor that will accept the given list of parameters (not requiring
     * an exact match on parameter types).  It turns any
     * errors into test case failures with appropriate hint messages.
     * Assumes the intended constructor should be public, and fails with an
     * appropriate hint if it is not.
     * Note that this method <b>does not handle variable argument lists</b>
     * in the target constructor for which it is searching.
     * @param c The type of object to create
     * @param params The constructor's parameter profile
     * @return The corresponding Constructor object
     */
    public static Constructor getMatchingConstructor(
        Class<?> c, Class ... params)
    {
        Constructor result = null;
        Constructor ctorWithSameParamCount = null;
        if (params == null) { params = new Class[0]; }
        for (Constructor m : c.getConstructors())
        {
            Class<?>[] paramTypes = m.getParameterTypes();
            if (params.length == paramTypes.length)
            {
                ctorWithSameParamCount = m;
                result = m; // maybe ... we'll clear it if wrong
                for (int i = 0; i < params.length; i++)
                {
                    if (params[i] != null)
                    {
                        // If the actual is non-null, check to see if
                        // it can be assigned to the formal correctly.
                        if (actualMatchesFormal(params[i], paramTypes[i]))
                        {
                            result = null;
                            break;
                        }
                    }
                    else if (paramTypes[i].isPrimitive())
                    {
                        // If actual is null, then the formal can't
                        // be a primitive
                        result = null;
                        break;
                    }
                }
                if (result != null)
                {
                    // If we found a match that can accept all the
                    // parameters  ...
                    break;
                }
            }
        }
        if (result == null)
        {
            String message = null;
            if (ctorWithSameParamCount != null)
            {
                message = "constructor cannot be called with argument"
                    + ((params.length == 1) ? "" : "s")
                    + " of type "
                    + simpleArgumentList(params)
                    + ": incorrect parameter type(s)";
            }
            else
            {
                message = "" + c + " is missing public constructor "
                    + simpleMethodName(simpleClassName(c), params);
            }
            fail(message);
        }
        else if (!Modifier.isPublic(result.getModifiers()))
        {
            fail("constructor " + simpleMethodName(simpleClassName(c), params)
                + " should be public");
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Just like {@link Constructor#newInstance(Object...)}, but converts
     * any thrown exceptions into RuntimeExceptions.
     * @param constructor The constructor to invoke
     * @param params The parameters to pass to the constructor
     * @return The newly created object
     */
    public static Object create(Constructor constructor, Object ... params)
    {
        Object result = null;
        try
        {
            result = constructor.newInstance(params);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e;
            while (cause.getCause() != null)
            {
                cause = cause.getCause();
            }
            throw new RuntimeException(cause);
        }
        catch (InstantiationException e)
        {
            Throwable cause = e;
            while (cause.getCause() != null)
            {
                cause = cause.getCause();
            }
            throw new RuntimeException(cause);
        }
        catch (IllegalAccessException e)
        {
            // This should never happen, since getMethod() has already
            // done the appropriate checks.
            throw new RuntimeException(e);
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Dynamically look up and invoke a class constructor for the target
     * class, with appropriate hints if any failures happen along the way.
     * @param returnType The type of object to create.
     * @param params The parameters to pass to the constructor
     * @param <T> The generic parameter T is deduced from the returnType
     * @return The newly created object
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(
        Class<T> returnType,
        Object ... params)
    {
        Object result = null;
        Class[] paramProfile = null;
        if (params != null)
        {
            paramProfile = new Class[params.length];
            for (int i = 0; i < params.length; i++)
            {
                if ( params[i] == null)
                {
                    // A null indicates we'll try to pass null as an
                    // actual in the getMatchingMethod() search
                    paramProfile[i] = null;
                }
                else
                {
                    paramProfile[i] = params[i].getClass();
                }
            }
        }
        Constructor c = getMatchingConstructor(returnType, paramProfile);

        result = create(c, params);

        if (result != null)
        {
            assertTrue("constructor "
                + simpleMethodName(simpleClassName(returnType), paramProfile)
                + " did not produce result of type "
                + simpleClassName(returnType),
                returnType.isAssignableFrom(result.getClass()));
        }
        // The cast below is technically unsafe, according to the compiler,
        // but will never be violated, due to the assertion above.
        return (T)result;
    }

}
