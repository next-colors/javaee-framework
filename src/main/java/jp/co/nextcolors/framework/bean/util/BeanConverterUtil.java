/*
 * Copyright (c) 2017 NEXT COLORS Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package jp.co.nextcolors.framework.bean.util;

import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import jp.co.nextcolors.framework.bean.annotation.BeanConverter;
import jp.co.nextcolors.framework.servlet.util.ServletContextUtil;

/**
 * Bean（JavaBeans）のコンバータに関するユーティリティです。
 *
 * @author hamana
 */
@UtilityClass
public class BeanConverterUtil
{
	//-------------------------------------------------------------------------
	//    Private Methods
	//-------------------------------------------------------------------------
	/**
	 * {@link ConvertUtilsBean} に登録するコンバータとコンバータでの変換の対象となるクラスの対応表を返します。
	 *
	 * @param context
	 * 			Servlet コンテキスト
	 * @return {@link ConvertUtilsBean} に登録するコンバータとコンバータでの変換の対象となるクラスの対応表
	 */
	private static Set<Pair<Class<? extends Converter>, Class<?>>> getConversionRelations( @NonNull final ServletContext context )
	{
		ImmutableSet.Builder<Pair<Class<? extends Converter>, Class<?>>> builder = ImmutableSet.builder();

		Reflections reflectionScanner = ServletContextUtil.getClassReflectionScanner( context );

		Set<Class<? extends Converter>> converters = reflectionScanner.getSubTypesOf( Converter.class );

		for ( Class<? extends Converter> converter : converters ) {
			if ( converter.isAnnotationPresent( BeanConverter.class ) ) {
				Class<?> targetClass = converter.getAnnotation( BeanConverter.class ).forClass();

				builder.add( Pair.of( converter, targetClass ) );
			}
		}

		return builder.build();
	}

	//-------------------------------------------------------------------------
	//    Public Methods
	//-------------------------------------------------------------------------
	/**
	 * {@link ConvertUtilsBean} にコンバータを登録します。
	 *
	 * @param context
	 * 			Servlet コンテキスト
	 * @see org.apache.commons.beanutils.ConvertUtils#register(Converter, Class)
	 */
	@SneakyThrows(ReflectiveOperationException.class)
	public static void registerConverters( @NonNull final ServletContext context )
	{
		Set<Pair<Class<? extends Converter>, Class<?>>> relations = getConversionRelations( context );

		for ( Pair<Class<? extends Converter>, Class<?>> relation : relations ) {
			Converter converter = ConstructorUtils.invokeConstructor( relation.getLeft() );
			Class<?> targetClass = relation.getRight();

			ConvertUtils.register( converter, targetClass );
		}
	}
}
