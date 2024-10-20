package org.jkiss.dbeaver.ext.cubrid.ui.controls;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.CustomSashForm;
import org.jkiss.dbeaver.ui.controls.lightgrid.IGridColumn;
import org.jkiss.dbeaver.ui.controls.lightgrid.IGridContentProvider;
import org.jkiss.dbeaver.ui.controls.lightgrid.IGridItem;
import org.jkiss.dbeaver.ui.controls.lightgrid.IGridLabelProvider;
import org.jkiss.dbeaver.ui.controls.lightgrid.IGridRow;
import org.jkiss.dbeaver.ui.controls.lightgrid.IGridStatusColumn;
import org.jkiss.dbeaver.ui.controls.resultset.AbstractPresentation;
import org.jkiss.dbeaver.ui.controls.resultset.IResultSetController;
import org.jkiss.dbeaver.ui.controls.resultset.ResultSetCopySettings;
import org.jkiss.dbeaver.ui.controls.resultset.spreadsheet.Spreadsheet;

public class InfoStatistic extends AbstractPresentation{
	private Composite control;
	private SashForm planPanel;
	private Spreadsheet spreadsheet;
	private Text sqlText;
	private SashForm leftPanel;
	@Override
    public void createPresentation(@NotNull final IResultSetController controller, @NotNull Composite parent) {
        this.controller = controller;
        
        control = UIUtils.createPlaceholder(parent, 1);
        control.setLayoutData(new GridData(GridData.FILL_BOTH));

        

        
        this.planPanel = new CustomSashForm(control, SWT.VERTICAL);
        this.planPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.planPanel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        final GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        this.planPanel.setLayout(gl);
        {
      
//        	leftPanel = UIUtils.createPartDivider(controller.getSite().getPart(), planPanel, SWT.VERTICAL);
//            leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        	sqlText = new Text(this.planPanel, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        	sqlText.setText("Table View");
        	Text t = new Text(this.planPanel, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        	t.setText("Statistic");
            
           


            //leftPanel.setMaximizedControl(planTree);
        }
       


        //planPanel.setMaximizedControl(planTree);

        
        
    }
	
	@Override
	public Control getControl() {
		// TODO Auto-generated method stub
		return control;
	}

	@Override
	public void refreshData(boolean refreshMetadata, boolean append, boolean keepState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatData(boolean refreshData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearMetaData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateValueView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeMode(boolean recordMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DBDAttributeBinding getCurrentAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Transfer, Object> copySelection(ResultSetCopySettings settings) {
		// TODO Auto-generated method stub
		return null;
	}

	 private class ContentProvider implements IGridContentProvider {

	       
			@Override
			public boolean hasChildren(IGridItem item) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Object[] getChildren(IGridItem item) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getCollectionSize(IGridColumn colElement, IGridRow rowElement) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getSortOrder(IGridColumn element) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public ElementState getDefaultState(IGridColumn element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public IGridStatusColumn[] getStatusColumns() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getColumnPinIndex(IGridColumn element) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean isElementSupportsFilter(IGridColumn element) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isElementSupportsSort(IGridColumn element) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isElementReadOnly(IGridColumn element) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isElementExpandable(IGridItem item) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isGridReadOnly() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void validateDataPresence(IGridColumn colElement, IGridRow rowElement) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public CellInformation getCellInfo(IGridColumn colElement, IGridRow rowElement, boolean selected) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isVoidCell(IGridColumn gridColumn, IGridRow gridRow) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Object getCellValue(IGridColumn colElement, IGridRow rowElement, boolean formatString) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getCellLinkText(IGridColumn colElement, IGridRow rowElement) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void resetColors() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object[] getElements(boolean horizontal) {
				// TODO Auto-generated method stub
				return null;
			}
	 }
	 
	 private class GridLabelProvider implements IGridLabelProvider {

		@Override
		public String getText(IGridItem element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDescription(IGridItem element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Image getImage(IGridItem element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getForeground(IGridItem element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getBackground(IGridItem element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getHeaderForeground(IGridItem item, boolean selected) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getHeaderBackground(IGridItem item, boolean selected) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getHeaderBorder(IGridItem item) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Font getFont(IGridItem element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getToolTipText(IGridItem element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getGridOption(String option) {
			// TODO Auto-generated method stub
			return null;
		}
		 
	 }

}
