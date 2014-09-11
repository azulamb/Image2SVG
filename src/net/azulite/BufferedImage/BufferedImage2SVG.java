package net.azulite.BufferedImage;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class BufferedImage2SVG
{
	public static boolean convert( File output, BufferedImage input, boolean trc_del )
	{
		BufferedImage2SVG svg;
		try
		{
			svg = new BufferedImage2SVG( new PrintWriter( new BufferedWriter( new FileWriter( output ) ) ), trc_del );
		} catch (IOException e)
		{
			return false;
		}

		svg.printSVGHeader( input.getWidth(), input.getHeight() );
		svg.printSVGBody( input );
		svg.printSVGFooter();

		return true;
	}

	private boolean trc_del;
	private PrintWriter pw;
	private int w, h;

	private BufferedImage2SVG( PrintWriter pw, boolean trc_delete )
	{
		this.pw = pw;
		trc_del = trc_delete;
	}
	private void printSVGHeader( int width, int height )
	{
		pw.println( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" );

		pw.print( "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" );
		pw.print( width );
		pw.print( "\" height=\"" );
		pw.print( height );
		pw.println( "\">" );
	}
	private void printSVGFooter()
	{
		pw.println( "</svg>" );
		pw.close();
	}
	private void printSVGBody( BufferedImage bmp )
	{
		int x, y, c, a;
		w = bmp.getWidth();
		h = bmp.getHeight();
		if ( trc_del )
		{
			for ( y = 0 ; y < h ; ++y )
			{
				for ( x = 0 ; x < w ;++x )
				{
					c = bmp.getRGB( x, y );
					a = alpha( c );
					if ( a == 0 ){ continue; }
					printDot( x, y, rgb( c ), a );
				}
			}
		} else
		{
			for ( y = 0 ; y < h ; ++y )
			{
				for ( x = 0 ; x < w ;++x )
				{
					c = bmp.getRGB( x, y );
					a = alpha( c );
					printDot( x, y, rgb( c ), a );
				}
			}
		}
	}
	private void printDot( int x, int y, String rgb, int a )
	{
		pw.print( "<rect width=\"1\" height=\"1\" x=\"" );
		pw.print( x );
		pw.print( "\" y=\"" );
		pw.print( y );
		pw.print( "\" " );
		pw.print( "style=\"fill:#" );
		pw.print( rgb );
		if ( a < 255 )
		{
			pw.print( ";fill-opacity:" );
			pw.print( a / 255.0 );
		}
		pw.println( ";\" />" );
	}
	private static String rgb( int c )
	{
		return String.format( "%02x%02x%02x", red( c ), green( c ), blue( c ) );
	}
	private static int alpha( int c ) { return c >>> 24; }
	private static int red( int c )   { return c >> 16 & 0xff; }
	private static int green(int c)   { return c >> 8  & 0xff; }
	private static int blue(int c)    { return c       & 0xff; }
}
