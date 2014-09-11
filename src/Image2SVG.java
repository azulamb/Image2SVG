import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.azulite.BufferedImage.BufferedImage2SVG;

public class Image2SVG extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static String ver = "1.0";

	public static void main( String[] args )
	{
		Image2SVG i2svg = new Image2SVG();
		i2svg.pack();
		i2svg.setVisible( true );
	}

	private JProgressBar loader;
	private JCheckBox ignore_trc;
	private JLabel success, failure, all;
	private int max, count_s;

	public Image2SVG()
	{
		super();

		this.setLayout( new BorderLayout() );
		this.setTitle( "Convert Image to SVG." );

		ClassLoader cl = this.getClass().getClassLoader();
		ImageIcon icon = new ImageIcon( cl.getResource( "back.png" ) );
		

		JPanel dpanel = new JPanel();
		dpanel.add( new JLabel( icon ) );//"Drop images here."
		dpanel.setPreferredSize( new Dimension( 400, 205 ) );
		///Border border = BorderFactory.createDashedBorder( Color.GRAY, 10, 5 );
		//dpanel.setBorder( border );

		JPanel option = new JPanel();
		option.setLayout( new GridLayout( 2, 3 ) );

		option.add( new JLabel( "Ignore transparent dot" ) );
		option.add( new JLabel( ":" ) );
		ignore_trc = new JCheckBox();
		ignore_trc.setSelected( true );
		option.add( ignore_trc );

		option.add( new JLabel( "Version" ) );
		option.add( new JLabel( ":" ) );
		option.add( new JLabel( ver ) );

		loader = new JProgressBar();

		JPanel status = new JPanel();
		status.setLayout( new GridLayout( 1, 3 ) );
		success = new JLabel( "Success:" );
		failure = new JLabel( "Failure:" );
		all = new JLabel( "" );
		status.add( success );
		status.add( failure );
		status.add( all );

		JPanel footer = new JPanel();
		footer.setLayout( new GridLayout( 2, 1 ) );
		footer.add( loader );
		footer.add( status );

		new DropTarget( dpanel, new DropImage( this ) );

		Container con = getContentPane();
		con.add( BorderLayout.NORTH, option );
		con.add( BorderLayout.CENTER, dpanel );
		con.add( BorderLayout.SOUTH, footer );

		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	}

	protected JProgressBar getLoader()
	{
		return loader;
	}
	protected boolean isIgnoreTRC()
	{
		return ignore_trc.isSelected();
	}
	protected void setMax( int max )
	{
		this.max = max;
		count_s = 0;
		all.setText( "0/" + max );
	}
	protected void setSuccess( int success )
	{
		count_s = success;
		this.success.setText( "Success:" + success );
		all.setText( success + "/" + max );
	}
	protected void setFailure( int failure )
	{
		this.failure.setText( "Failure:" + failure );
	}
	protected void complete()
	{
		all.setText( "Complete:" + count_s + "/" + max );
	}
}

class DropImage extends DropTargetAdapter
{
	private Image2SVG frame;
	private JProgressBar loader;

	public DropImage( Image2SVG frame )
	{
		this.frame = frame;
		this.loader = frame.getLoader();
	}

	public void drop( DropTargetDropEvent e)
	{
		try
		{
			Transferable t = e.getTransferable();
			if ( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) )
			{
				e.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );
				@SuppressWarnings("unchecked")
				List<File> filelist = (java.util.List<File>) 
				(t.getTransferData(DataFlavor.javaFileListFlavor));

				convert( filelist, frame.isIgnoreTRC() );
			}
		} catch( Exception ex )
		{
			// TODO
		}
	}

	public void convert( List<File> filelist, boolean ignore_trc )
	{
		int max = filelist.size();
		frame.setMax( max );
		loader.setMaximum( max );

		int success = 0;
		int failure = 0;
		frame.setSuccess( 0 );
		frame.setFailure( 0 );
		for ( File file: filelist )
		{
			try
			{
				BufferedImage src = ImageIO.read( file );
				BufferedImage cnv = new BufferedImage( src.getWidth(), src.getHeight(), BufferedImage.TYPE_4BYTE_ABGR );
				cnv.getGraphics().drawImage( src, 0, 0, null);

				if ( BufferedImage2SVG.convert( newSVGFile( file ), cnv, ignore_trc ) )
				{
					loader.setValue( ++success );
					frame.setSuccess( success );
				} else
				{
					loader.setMaximum( --max );
					frame.setFailure( ++failure );
				}
			} catch ( Exception e )
			{
				// TODO
				loader.setMaximum( --max );
				frame.setFailure( ++failure );
			}
		}
		loader.setValue( 0 );
		frame.complete();
	}

	public static File newSVGFile( File file )
	{
		String path = file.getPath();
		int lastDotPos = path.lastIndexOf('.');

		if (lastDotPos <= 0 ) {
			path += ".svg";
		} else
		{
			path = path.substring( 0, lastDotPos ) + ".svg";
		}
		return new File( path );
	}
}
