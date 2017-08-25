package jp.co.nextcolors.framework.bean.converter;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import jp.co.nextcolors.framework.bean.annotation.BeanConverter;

/**
 * {@link Date} 型の日付に変換するためのコンバータです。
 *
 * @author hamana
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@BeanConverter(forClass = Date.class)
public class DateConverter extends DateTimeConverter
{
	//-------------------------------------------------------------------------
	//    Private Constants
	//-------------------------------------------------------------------------
	/**
	 * 日付のコンポーネントです。
	 *
	 */
	private static final String[] DATE_COMPONENTS =
			StringUtils.split( DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.getPattern(), '-' );

	/**
	 * 時刻のコンポーネントです。
	 *
	 */
	private static final String[] TIME_COMPONENTS =
			StringUtils.split( DateFormatUtils.ISO_8601_EXTENDED_TIME_FORMAT.getPattern(), ':' );

	/**
	 * タイムゾーンのコンポーネントです。
	 *
	 */
	private static final String[] TIME_ZONE_COMPONENTS = { StringUtils.EMPTY, "Z", "ZZ", "ZZZ" };

	/**
	 * 日付のセパレータです。
	 *
	 */
	private static final String[] DATE_SEPARATORS = { StringUtils.EMPTY, "-", "/" };

	/**
	 * 時刻のセパレータです。
	 *
	 */
	private static final String[] TIME_SEPARATORS = { StringUtils.EMPTY, ":" };

	/**
	 * 日付と時刻のセパレータです。
	 *
	 */
	private static final String[] DATE_TIME_SEPARATORS = { StringUtils.EMPTY, StringUtils.SPACE, "'T'" };

	//-------------------------------------------------------------------------
	//    Private Methods
	//-------------------------------------------------------------------------
	/**
	 * 日付のフォーマットを取得します。
	 *
	 * @return 日付のフォーマット
	 */
	private Set<String> getDateFormats()
	{
		return Arrays.stream( DATE_SEPARATORS )
						.map( separator -> String.join( separator, DATE_COMPONENTS ) )
						.collect( ImmutableSet.toImmutableSet() );
	}

	/**
	 * 時刻のフォーマットを取得します。
	 *
	 * @return 時刻のフォーマット
	 */
	private Set<String> getTimeFormats()
	{
		Set<String> timeFormats = Sets.newHashSet();

		for ( String separator : TIME_SEPARATORS ) {
			for ( int i = 1; i <= TIME_COMPONENTS.length; ++i ) {
				String[] timeComponents = Arrays.copyOfRange( TIME_COMPONENTS, 0, i );

				String timeFormat = String.join( separator, timeComponents );

				for ( String timeZoneComponent : TIME_ZONE_COMPONENTS ) {
					timeFormats.add( timeFormat + timeZoneComponent );
				}
			}
		}

		return ImmutableSet.copyOf( timeFormats );
	}

	/**
	 * 日時のフォーマットを取得します。
	 *
	 * @return 日時のフォーマット
	 */
	private String[] getDateTimeFormats()
	{
		Set<String> dateFormats = getDateFormats();
		Set<String> timeFormats = getTimeFormats();

		Set<String> dateTimeFormats = Sets.newHashSet();
		dateTimeFormats.addAll( dateFormats );

		for ( String dateFormat : dateFormats ) {
			for ( String timeFormat : timeFormats ) {
				for ( String separator : DATE_TIME_SEPARATORS ) {
					dateTimeFormats.add( String.join( separator, dateFormat, timeFormat ) );
				}
			}
		}

		return dateTimeFormats.stream().toArray( String[]::new );
	}

	//-------------------------------------------------------------------------
	//    Protected Methods
	//-------------------------------------------------------------------------
	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	protected Class<Date> getDefaultType()
	{
		return Date.class;
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	protected <T> T convertToType( @NonNull final Class<T> type, @NonNull final Object value ) throws Exception
	{
		if ( !String.class.isInstance( value ) ) {
			return super.convertToType( type, value );
		}

		try {
			Date date = DateUtils.parseDateStrictly( Objects.toString( value ), getPatterns() );

			return type.cast( date );
		}
		catch ( ParseException e ) {
			throw new ConversionException( String.format( "%s を %s に変換できませんでした。使用した日時フォーマットは %s です。",
															value, type.getName(), Arrays.toString( getPatterns() ) ),
											e );
		}
	}

	//-------------------------------------------------------------------------
	//    Public Methods
	//-------------------------------------------------------------------------
	public DateConverter()
	{
		super( null );
		setPatterns( getDateTimeFormats() );
	}
}
