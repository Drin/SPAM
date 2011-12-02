package spam.dataTypes;

public class Matrix<T> {
   //private T[][] dataTable = null;
   private ArrayList<ArrayList<T>> dataTable = null;
   private ArrayList<String> rowHeaders = null, colHeaders = null;

   public Matrix() {
      dataTable = new ArrayList<ArrayList<T>>();
      rowHeaders = new ArrayList<String>();
      colHeaders = new ArrayList<String>();
   }

   public void set(int row, int col, double val) {
      dataTable.get(row).set(col, val);
   }

   public void addRow(ArrayList<T> dataRow) {
      dataTable.add(dataRow);
   }

   public void addCol(ArrayList<T> dataCol) {
      int colNdx = 0;

      for (ArrayList<T> row : dataTable) {
         row.add(dataCol.get(colNdx++));
      }
   }

   public void setRowHeaders(ArrayList<String> headers) {
      rowHeaders = headers;
   }

   public void addRowHeaders(ArrayList<String> headers) {
      rowHeaders.add(headers);
   }

   public void setColHeaders(ArrayList<String> headers) {
      colHeaders = headers;
   }

   public void addColHeaders(ArrayList<String> headers) {
      colHeaders.add(headers);
   }

   public int getNumRows() {
      return dataTable.size();
   }

   public int getNumCols() {
      return dataTable.get(0).size();
   }
}
