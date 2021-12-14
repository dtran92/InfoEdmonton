// This sample uses the Place Autocomplete widget to allow the user to search
// for and select a place. The sample then displays an info window containing
// the place ID and other information about the place that the user has
// selected.
// This example requires the Places library. Include the libraries=places
// parameter when you first load the API. For example:
// <script src="https://maps.googleapis.com/maps/api/js?key=YOUR_API_KEY&libraries=places">

function initMap() {
  const map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: 53.5461, lng: -113.4938  },
    zoom: 11,
  });

}
function findAddress(latitude, longitude, accountNum, address, assessedValue){
    const map = new google.maps.Map(document.getElementById("map"), {
    center: { lat: 53.5461, lng: -113.4938  },
    zoom: 11,
  });
  const marker = new google.maps.Marker({ map: map,
     position:{lat: parseFloat(latitude), lng: parseFloat(longitude) }});


  const infowindow = new google.maps.InfoWindow();
  const infowindowContent = document.getElementById("infowindow-content");
  infowindow.setContent(infowindowContent);

    // Create our number formatter.
    var formatter = new Intl.NumberFormat('en-CA', {
        style: 'currency',
        currency: 'CAD',
        maximumFractionDigits: 0,
        minimumFractionDigits: 0,
    });

  marker.setVisible(true);
  infowindowContent.children.namedItem("place-id").textContent = accountNum;
  infowindowContent.children.namedItem("place-address").textContent = address;

  infowindowContent.children.namedItem("place-value").textContent = formatter.format(assessedValue);
  infowindow.open(map, marker);
}
function showProperty(propertyObj){
    const map = new google.maps.Map(document.getElementById("map"), {
        center: { lat: 53.5461, lng: -113.4938  },
        zoom: 11,
      });

    var collectionOfProperties = propertyObj;
    collectionOfProperties = collectionOfProperties.replace(/[\[\]]/g, '');
    var propertyArray = collectionOfProperties.split(',');
    var len = propertyArray.length;

    for(var i =0; i< len;i++)
    {
        const propertyInfo = propertyArray[i].split('|');
        const marker = new google.maps.Marker({ map: map,
            position:{lat: parseFloat(propertyInfo[3]), lng: parseFloat(propertyInfo[4]) }});

        var infowindow = new google.maps.InfoWindow();
        google.maps.event.addListener(marker, 'click', (function(marker, i) {
            return function() {

                const infowindowContent = document.getElementById("infowindow-content");
                infowindow.setContent(infowindowContent);

                const formatter = new Intl.NumberFormat('en-CA', {
                      style: 'currency',
                      currency: 'CAD',
                      maximumFractionDigits: 0,
                      minimumFractionDigits: 0,
                      });

                infowindowContent.children.namedItem("place-id").textContent = propertyInfo[1];
                infowindowContent.children.namedItem("place-address").textContent = propertyInfo[2];

                infowindowContent.children.namedItem("place-value").textContent = formatter.format(propertyInfo[0]);
                infowindow.open(map, marker);
            }
        })(marker, i));
    }

}

function createPoly(coorList){
    const map = new google.maps.Map(document.getElementById("map"), {
      center: { lat: 53.5461, lng: -113.4938  },
      zoom: 11,
    });
    var tempString = coorList.replace(/[\[\]()]/g, '');
    var tempArray = tempString.split(',');
    var polyShape = [];
    var len = tempArray.length;
    for(var i = 0; i < len; i+=2) {
        polyShape.push({lat:parseFloat(tempArray[i]),lng:parseFloat(tempArray[i+1])});
    }


    const mapShape = new google.maps.Polygon({
        path: polyShape,
            strokeColor: "#FF0000",
            strokeOpacity: 0.8,
            strokeWeight: 2,
            fillColor: "#FF0000",
            fillOpacity: 0.35,
    });
    mapShape.setMap(map);


}