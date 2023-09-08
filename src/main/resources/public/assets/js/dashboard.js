function myFunction() {
  // Declare variables
    var input, filter, table, tr, td, i, txtValue;
    input = document.getElementById("tableInput");
    filter = input.value.toUpperCase();
    table = document.getElementById("pluginTable");
    tr = table.getElementsByTagName("tr");

    for (i = 0; i < tr.length; i++) {
      var displayRow = false; // Flag to determine if the row should be displayed

      // Loop through all columns in the current row
      for (j = 0; j < tr[i].cells.length; j++) {
        td = tr[i].cells[j];
        if (td) {
          txtValue = td.textContent || td.innerText;
          if (txtValue.toUpperCase().indexOf(filter) > -1) {
            displayRow = true; // Set the flag to true if the filter text is found in any column
            break; // No need to check other columns, so exit the loop
          }
        }
      }

      // Set the row's display property based on the flag
      tr[i].style.display = displayRow ? "" : "none";
    }
}


function populateTable(data) {
  var table = document.getElementById("pluginTable");

  for (var i = 0; i < data.length; i++) {
    var row = table.insertRow(-1); // Insert a new row at the end of the table

    // Insert cells into the row and populate them with data
    var idCell = row.insertCell(0);
    var nameCell = row.insertCell(1);
    var typeCell = row.insertCell(2);
    var vendorCell = row.insertCell(3);
    var versionsCountCell = row.insertCell(4);


    idCell.innerHTML = data[i].id;
    nameCell.innerHTML = data[i].name;
    typeCell.innerHTML = data[i].type;
    vendorCell.innerHTML = data[i].vendor;
    versionsCountCell.innerHTML = data[i].versions_count;
  }
}

// Function to fetch data from the API
function fetchDataAndPopulateTable() {
  fetch('/v1/plugins-table')
    .then(response => response.json())
    .then(data => {
      populateTable(data);
    })
    .catch(error => {
      console.error('Error fetching data:', error);
    });
}

// Call the function to fetch and populate data when the page loads
window.onload = function () {
  fetchDataAndPopulateTable();
};