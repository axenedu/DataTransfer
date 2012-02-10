
/*
 * Copyright (C) 2011, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:jariera@emergya.com">José Alfonso Riera</a>
 * @author <a href="mailto:jsoler@emergya.com">Jaime Soler</a>
 * @author <a href="mailto:eserrano@emergya.com">Eduardo Serrano</a>
 *
 * This file is Component DataTransfer
 *
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */

package com.emergya.aplicaciones.datatransfer;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;

/**
 * Class DataTransfer
 * @author <a href="mailto:eserrano@emergya.com">Eduardo Serrano Luque</a>
 * @author <a href="mailto:jsoler@emergya.com">Jaime Soler</a>
 * @author <a href="mailto:jariera@emergya.com">José Alfonso Riera</a>
 * @author <a href="mailto:frodriguez@emergya.com">Francisco Rodríguez Mudarra</a>
 * @version 0.2
 * 
 * The functionality of the class is as follows:

    1. Copy full source object (src) to an object to an object (target), the objects need not be the same type, if you can not copy the whole object 
    should throw an exception, the copy is based on copying all get method attributes with the target object  with the same attributes set method 
    (same type) in the destination. This function can be called exactCopy.
   
    2. Complete copy in a target object from the values ​​of a source object, need not be the same type, all method attributes will get the target object
    must match an attribute with get method in the source (same type) in otherwise raise an exception. This method could be called completCopy.
   
    3. Partial copy a target object from a source object, need not be the same type for all attributes of the target object method get to try to find 
    a get method that corresponds to the source object, if it is simply not copy that attribute. This method could be called partialCopy.
   
    4. Copy of request attributes in a target object is copied the attributes specified in a set of strings (Collection) of a target object. 
    If there is some method get (the source) or set (destination) of the specified attributes, the method throws an exception.
    This method could be called customCopy.




   To  the extent necessary to cut some restrictions:
    1. Data copying will not consider getters with an interface as the type of attributes, only  instantiable classes.
    2. Collection Data Types and derivatives will be ignored.
    3. A copy of 2 attributes that have different types (Example Entity getProiedadX () to setPropiedadX (Disc property) is done by
       creating an instance of the target attribute and applying the same copy that the object containing the property.
 
 * This file is Component StandardBreadCrumb
 *
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */

public class DataTransfer {
	
	/** Prefix get method */
	private static final String METHOD_GET = "get";
		
	/** First character getter */
	private static final String FIRST_METHOD_GET = "g";
	
	/** First character setter */
	private static final String FIRST_METHOD_SET = "s";
	
	/** Represents the value of static change */
	private static final int CTE_PUBLIC_STATIC_FINAL = 25;
	
	/** Represents the value of static change */
	private static final int CTE_PRIVATE_STATIC_FINAL = 26;
	
	/** Represents the value of static change */
	private static final int CTE_PRIVATE_FINAL = 18;
	
	
	
	
	/**
	 * exactCopy
	 * 
	 * Copy full source object (src) to an object to an object (target)
	 * Objects need not be the same type, if you can not copy the whole object
	 * Should throw an exception, based on the copy to copy all the attributes of the object source get method
	 * To the same attribute set method (same type) in the destination.
	 * 
	 * @param srcClass Class
	 * @param Object In src
	 * @param Object Out target
	 * @return target reference
	 * @throws DataTransferException
	 */
	public static Object exactCopy(Class<? extends Object> srcClass, Object src, Object target) throws DataTransferException {

		try{		
						
			// Class Object target
			Class<? extends Object> targetClass = target.getClass();
						
		
			Field [] attributes = srcClass.getDeclaredFields();
			Method [] methods = srcClass.getMethods();
				
			for(int i = 0; i < attributes.length; i++){
				Field attribute = attributes[i];
				String fieldname = attribute.getName();
				
				int v = attribute.getModifiers();
				
				String methodSearch = METHOD_GET + fieldname.replaceFirst(fieldname.substring(0, 1), fieldname.substring(0, 1).toUpperCase());
				String methodSetSearch = methodSearch.replaceFirst(FIRST_METHOD_GET, FIRST_METHOD_SET);
						
				int indexMethod = haveMethod(methodSearch, methods);
				int indexMethodSet = haveMethod(methodSetSearch, targetClass.getMethods());
				
				
				if(!isEstatic(v)){
					
					//If the  method copies the value exists, otherwise exception
					if(indexMethod != -1 && esTipoBasico(attribute.getType())){
						
						Object value = invokeGet(methods, indexMethod, src);
						
						//Ignored collections
						if(!esCollection(value)){
							if(indexMethodSet != -1){
								Object [] args = {value};
								invoke(targetClass, indexMethodSet, target, args);
							}else{
								throw new DataTransferException("Error in exactCopy.Method does not exist: " + methodSearch);
							}
						}
						
					}else{
						throw new DataTransferException("Error in exactCopy.Method does not exist: " + methodSearch);
					}
				}
			}
		return target;
		}catch(Exception e){
			throw new DataTransferException("Error in exactCopy", e);
		}
		
	}
	
	
	
	
	
	/**
	 * partialCopy
	 * 
	 * In a target object from a source object, it must be of the same type,
	 * For all attributes of the target object method get to try to find a get method that corresponds to the source object,
	 * If it is not simply that attribute is not copied.
	 * 
	 * @param srcClass Class
	 * @param Object In src
	 * @param Object Out target
	 * @return target reference
	 * @throws DataTransferException
	 */
	public static Object partialCopy(Class<? extends Object> srcClass, Object src, Object target) throws DataTransferException {
		
		
		try{		
						
			// Class Object target
			Class<? extends Object> targetClass = target.getClass();
						
		
			Field [] attributes = srcClass.getDeclaredFields();
			Method [] methods = srcClass.getMethods();
				
			for(int i = 0; i < attributes.length; i++){
				Field attribute = attributes[i];
				String fieldname = attribute.getName();
				
				String methodSearch = METHOD_GET + fieldname.replaceFirst(fieldname.substring(0, 1), fieldname.substring(0, 1).toUpperCase());
				String methodSetSearch = methodSearch.replaceFirst(FIRST_METHOD_GET, FIRST_METHOD_SET);
						
				int indexMethod = haveMethod(methodSearch, methods);
				int indexMethodSet = haveMethod(methodSetSearch, targetClass.getMethods());
				
				int v = attribute.getModifiers();
				
				//If the  method copies the value exists, otherwise it is ignored
				if(indexMethod != -1 && esTipoBasico(attribute.getType())){
					if(!isEstatic(v)){
						Object value = invokeGet(methods, indexMethod, src);
						
						//Ignored collections
						if(!esCollection(value)){
							if(indexMethodSet != -1){
								Object [] args = {value};
								invoke(targetClass, indexMethodSet, target, args);
							}
						}
					}
				}
				
			}
			
		return target;
		}catch(Exception e){
			throw new DataTransferException("Error in partialCopy", e);
		}
	}
	
	
	
	
	
	/**
	 * customCopy
	 *
	 * A target object in the specified attributes will be copied into a set of strings (Collection) of a target object. 
	 * If there is some method get (the source) or set (destination) of the specified attributes, the method throws an exception.
	 *
	 * @param srcClass Class
	 * @param Object In src
	 * @param Object Out target
	 * @param atributtes
	 * @return target reference
	 * @throws DataTransferException
	 */
	public static Object customCopy(Class<? extends Object> srcClass, Object src, Object target, Collection<String> attributes) throws DataTransferException {
		
		
		try{
			Iterator<String> it;
			String attribute;
			
			if(attributes != null 
					&& attributes.size() > 0){
				
				it = attributes.iterator();
							
				// Class Object target
				Class<? extends Object> targetClass = target.getClass();
				
				
				//Iterate over the collection of attributes
				while(it.hasNext()){
					attribute = it.next();
					
					if(attribute instanceof String){
					
						String methodSearch = METHOD_GET + attribute.replaceFirst(attribute.substring(0, 1), attribute.substring(0, 1).toUpperCase());
						String methodSetSearch = methodSearch.replaceFirst(FIRST_METHOD_GET, FIRST_METHOD_SET);
						
						Method [] methods = srcClass.getMethods();
						
						int indexMethod = haveMethod(methodSearch, methods);
						int indiceMetodoSet = haveMethod(methodSetSearch, targetClass.getMethods());
							
							
						//If the  method copies the value exists, otherwise it is ignored
						if(indexMethod != -1 ){		
								Object value = invokeGet(methods, indexMethod, src);
										
								//Ignored collections
								if(!esCollection(value)){
									if(indiceMetodoSet != -1){
										Object [] args = {value};
										invoke(targetClass, indiceMetodoSet, target, args);
									}else{
										throw new DataTransferException("Error in customCopy.Method does not exist: " + methodSearch);
									}
								}	
						}else{
							throw new DataTransferException("Error in customCopy.Method does not exist: " + methodSearch);
						}
							
						
					}else{
						throw new DataTransferException("Error in customCopy.");
					}
				}
			}
			
		return target;
		}catch(Exception e){
			throw new DataTransferException("Error in customCopy", e);
		}
	}

	
	
	
	
	/**
	 * Copia completa en un objeto destino de los valores de un objeto fuente, 
	 * no tienen que ser del mismo tipo, a todos los atributos con método get del objeto destino le debe corresponder 
	 * un atributo con método get en el fuente (del mismo tipo), en caso contrario lanzará una excepción. 
	 * Este método se podría denominar copiaCompleta.
	 * 
	 * completCopy
	 * 
	 * Complete copy in a target object from the values ​​of a source object, need not be the same type, all method attributes 
	 * will get the target object must match an attribute with get method in the source (same type) in otherwise raise an exception. 
	 * 
	 * @param srcClass Class
	 * @param src
	 * @param target
	 * @return Object
	 * @return target reference
	 * @throws DataTransferException
	 */
	public static Object completCopy(Class<? extends Object> srcClass, Object src, Object target) throws DataTransferException {
		
		int size = 0;
		
		try{		
						
			// Class Object target
			Class<? extends Object> targetClass = target.getClass();
		
			Field [] attributes = srcClass.getDeclaredFields();
			Method [] methods = srcClass.getMethods();
			
			Field [] targetAttributes = targetClass.getDeclaredFields();
			
			//Number of attributes of the target  class
			size = targetAttributes != null ? targetAttributes.length : 0;
			
			for(int i = 0; i < attributes.length; i++){
				Field atributte = attributes[i];
				String fieldname = atributte.getName();
				
				String methodSearch = METHOD_GET + fieldname.replaceFirst(fieldname.substring(0, 1), fieldname.substring(0, 1).toUpperCase());
				String methodSetSearch = methodSearch.replaceFirst(FIRST_METHOD_GET, FIRST_METHOD_SET);
						
				int indexMethod = haveMethod(methodSearch, methods);
				int indexMethodSet = haveMethod(methodSetSearch, targetClass.getMethods());
				
				
				if(indexMethod == -1 && indexMethodSet !=- 1){
					throw new DataTransferException("Error in completCopy.Method does not exist: " + methodSearch);
				}
				
				
				 if(indexMethod != -1 && indexMethodSet != -1){
					
					//If the  method copies the value exists, otherwise exception
					if(indexMethod != -1){
						
						Object value = invokeGet(methods, indexMethod, src);
						
						//Ignored collections
						if(!esCollection(value)){
							if(indexMethodSet != -1){
								Object [] args = {value};
								invoke(targetClass, indexMethodSet, target, args);
								if(size > 0)
									size--;
							}
						}else{
							if(size > 0)
								size--;
						}
						
					}else{
						throw new DataTransferException("Error in completCopy.Method does not exist: " + methodSearch);
					}
				}else{
					
				}
			}
		
		}catch(Exception e){
			throw new DataTransferException("Error in completCopy", e);
		}
		
		
		if(size > 0)
			throw new DataTransferException("Failed to make every set of the target");

		return target;
	}
	

	
	
	/**
	 * Indicates whether the switch is static
	 * @param v int
	 * @return true if the static modifier is static
	 */
	private static boolean isEstatic(int v){
		boolean b = true;
		
		if(CTE_PUBLIC_STATIC_FINAL != v && 
				CTE_PRIVATE_STATIC_FINAL != v && 
				CTE_PRIVATE_FINAL != v){
			
			b = false;
		}
		
		return b;
	}
	
	
	
	/**
	* Method which invokes methods without arguments
	* @param methods
	* @param indiceMetodo
	* @param invoked
	* @return Object
	* @throws DataTranferException
	*/
	private static Object invokeGet(Method [] methods, int indexMethod, Object invoke) throws Exception{
		try {
			return methods[indexMethod].invoke(invoke, null);
		} catch (IllegalArgumentException e1) {
			throw new Exception("Error in the arguments of the class get method " + invoke.getClass().getName(), e1);
		} catch (IllegalAccessException e1) {
			throw new Exception("Error in the get method of the class" + invoke.getClass().getName(), e1);
		} catch (InvocationTargetException e1) {
			throw new Exception( "Error invoking class" + invoke.getClass().getName(), e1);
		}
	}
	
	

	
	
	/**
	* Methods that invoke methods
	* @param class
	* @param indexMethod
	* @param invoked
	* @param args
	* @return Object
	* @throws DataTranferException
	*/
	private static Object invoke(Class<? extends Object> c, int indexMethod, Object invoke, Object[] args) throws Exception{
		try {
			return c.getMethods()[indexMethod].invoke(invoke, args);
		} catch (IllegalArgumentException e) {
			throw new Exception("Error in the arguments of the class get method " + c.getName(), e);
		} catch (SecurityException e) {
			throw new Exception("Security error when invoking the set method of the class " + c.getName(), e);
		} catch (IllegalAccessException e) {
			throw new Exception("Error in the set method of the class " + c.getName(), e);
		} catch (InvocationTargetException e) {
			throw new Exception("Error invoking class" + c.getName(), e);
		}
	}
	
	
	/**
	* Method that creates new instances of the class to be passed
	* @param c Class
	* @return Object
	* @throws DataTranferException
	*/
	private static Object newInstance(Class<? extends Object> c) throws Exception{
		try {
			return c.newInstance();
		} catch (InstantiationException e1) {
			throw new Exception( "Error instanceof class" + c.getName(), e1);
		} catch (IllegalAccessException e1) {
			throw new Exception( "Error instanceof class" + c.getName(), e1);
		}
		
	}
	
	
	
	/**
	* Checks for a method in an array of methods, returning
	* The position of the array method, if the method is not found within
	* The function returns -1
	* @param methodName
	* @param methods
	* @return int
	*/
	private static int haveMethod(String methodName, Method[] methods){
		
		int tiene = -1;
		for(int i = 0; i < methods.length; i++){
			Method method = methods[i];
			if(method.getName().equals(methodName)){
				tiene = i;
				break;
			}
		}
		
		return tiene;
	}

	
	/**
	* Method that checks if a class is a basic type mapping
	* @param c class to check
	* @return boolean true if it is a basic type or false otherwise
	*/
	private static boolean esTipoBasico(Class<? extends Object> c) {
		return (c.isAssignableFrom(Long.class) || 
				c.isAssignableFrom(Double.class) || 
				c.isAssignableFrom(String.class) ||
				c.isAssignableFrom(Boolean.class) || 
				c.isAssignableFrom(Calendar.class) ||
				c.isAssignableFrom(Character.class) ||
				c.isAssignableFrom(Byte.class) || 
				c.isAssignableFrom(Short.class) || 
				c.isAssignableFrom(Integer.class) || 
				c.isAssignableFrom(GregorianCalendar.class) ||
				c.isAssignableFrom(Date.class)) ||
				c.isPrimitive();
	}
	
	
	
	
	/**
	* Method that checks if a class is a basic type mapping
	* @param c class to check
	* @return boolean true if it is a basic type or false otherwise
	*/
	private static boolean esCollection(Object c) {
		return c instanceof Collection;
	}
	
}

