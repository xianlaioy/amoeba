/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.config;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.util.StringUtil;



/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * @version $Id: ParameterMapping.java 4004 2007-05-31 03:27:21Z struct $
 */
public class ParameterMapping {
	private static Logger logger = Logger.getLogger(ParameterMapping.class);
	private static Map<Class<?>,PropertyDescriptor[]> propertyDescriptorMap = new HashMap<Class<?>,PropertyDescriptor[]>();

	private static PropertyDescriptor[] getDescriptors(Class<?> clazz) {
		PropertyDescriptor[] descriptors;
		List<PropertyDescriptor> list;
		PropertyDescriptor[] mDescriptors = (PropertyDescriptor[]) propertyDescriptorMap
				.get(clazz);
		if (null == mDescriptors) {
			try {
				descriptors = Introspector.getBeanInfo(clazz)
						.getPropertyDescriptors();
				list = new ArrayList<PropertyDescriptor>();
				for (int i = 0; i < descriptors.length; i++){
					if (null != descriptors[i].getPropertyType()){
							list.add(descriptors[i]);
					}
				}
				mDescriptors = new PropertyDescriptor[list.size()];
				list.toArray(mDescriptors);
			} catch (IntrospectionException ie) {
				ie.printStackTrace();
				mDescriptors = new PropertyDescriptor[0];
			}
		}
		propertyDescriptorMap.put(clazz, mDescriptors);
		return (mDescriptors);
	}

	public static void mappingObject(Object object, Map<String,Object> parameter) {
		PropertyDescriptor[] descriptors = getDescriptors(object.getClass());

		for (int i = 0; i < descriptors.length; i++) {
			
			Object obj = parameter.get(descriptors[i].getName());
			Object value = null;
			Class<?> cls = descriptors[i].getPropertyType();
			if(obj instanceof String){
				String string = (String) obj;
				if (!StringUtil.isEmpty(string)) {
					string = ConfigUtil.filter(string);
				}
				
				if(isPrimitiveType(cls)){
					value = deStringize(cls, string);
				}
			}else if(obj instanceof BeanObjectEntityConfig){
				
				value = newBean((BeanObjectEntityConfig)obj);
				
			}else if(obj instanceof BeanObjectEntityConfig[]){
				List<Object> list = new ArrayList<Object>();
				
				for(BeanObjectEntityConfig beanconfig:(BeanObjectEntityConfig[])obj){
					list.add(newBean(beanconfig));
				}
				value = list.toArray();
			}
			
			if (cls != null) {
				try {
					if (value != null) {
						Method method = descriptors[i].getWriteMethod();
						if(method != null){
							method.invoke(object, new Object[] { value });
						}else{
							/*object.getClass().getMethod(name, parameterTypes)
							if()*/
							logger.info(object.getClass()+"@"+descriptors[i].getName()+" can not write able");
						}
					}
				} catch (Throwable t) {
					// ignore
				}
			}
		}
	}
	

	@SuppressWarnings("unchecked")
	public  static  Object newBean(BeanObjectEntityConfig beanConfig){
		Object beanvalue = beanConfig.createBeanObject(true);
		//Map bean
		if(beanvalue instanceof Map){
			Map map = (Map)beanvalue;
			for(Map.Entry<String, Object> entry:beanConfig.getParams().entrySet()){
				String key = entry.getKey();
				Object mapValue = entry.getValue();
				if(mapValue instanceof BeanObjectEntityConfig){
					BeanObjectEntityConfig mapBeanConfig = (BeanObjectEntityConfig)entry.getValue();
					mapValue = mapBeanConfig.createBeanObject(true);
					mappingObject(mapValue,mapBeanConfig.getParams());
				}
				map.put(key, mapValue);
			}
		}else if(beanvalue instanceof List){
			
		}
		//other bean
		else{
			mappingObject(beanvalue,beanConfig.getParams());
		}
		
		return beanvalue;
	}

	/**
	 * Convert the given string into an acceptable object for the property
	 * setter.
	 * 
	 * @param cls
	 *            The class determined from the bean information.
	 * @param string
	 *            The value to be assigned to the property (as a
	 *            <code>String</code>).
	 * @return An object suitable for assignment that has the implied value of
	 *         the string, or <code>null</code> if no conversion was possible.
	 */
	private static Object deStringize(Class<?> cls, String string) {
		Method method;
		Object value = null;

		if (cls.equals(String.class)) {
			value = string;
		} else if (cls.equals(Boolean.TYPE)) {
			value = Boolean.valueOf(string);
		} else if (cls.equals(Byte.TYPE)) {
			value = Byte.valueOf(string);
		} else if (cls.equals(Short.TYPE)) {
			value = Short.valueOf(string);
		} else if (cls.equals(Integer.TYPE)) {
			value = Integer.valueOf(string);
		} else if (cls.equals(Long.TYPE)) {
			value = Long.valueOf(string);
		} else if (cls.equals(Double.TYPE)) {
			value = Double.valueOf(string);
		} else if (cls.equals(Float.TYPE)) {
			value = Float.valueOf(string);
		} else if ((cls.equals(Boolean.class)) || (cls.equals(Byte.class))
				|| (cls.equals(Short.class)) || (cls.equals(Integer.class))
				|| (cls.equals(Long.class)) || (cls.equals(Float.class))
				|| (cls.equals(Double.class))) {
			try {
				method = cls.getMethod("valueOf", new Class[] { String.class });
				value = method.invoke(null, new Object[] { string });
			} catch (Throwable t) {
				value = null; // oh well, we tried
			}
		} else if(cls.equals(Class.class)){
			try {
				value = Class.forName(string);
			} catch (ClassNotFoundException e) {
				logger.error(string+" class not found",e);
			}
		}
		else {
			value = null;
		}

		return (value);
	}
	
	
	private static boolean isPrimitiveType(Class<?> cls){
		if (cls.equals(String.class) || cls.equals(Boolean.TYPE) || cls.equals(Byte.TYPE)
			|| cls.equals(Short.TYPE)|| cls.equals(Integer.TYPE)|| cls.equals(Long.TYPE)
			     ||cls.equals(Double.TYPE) || cls.equals(Float.TYPE)
			     || cls.equals(Boolean.class) || cls.equals(Byte.class)
				|| cls.equals(Short.class) || cls.equals(Integer.class)
				|| cls.equals(Long.class) || cls.equals(Float.class)
				|| cls.equals(Double.class) || cls.equals(Class.class)) {
			return true;
		}else{
			return false;
		}
		
	}
}
