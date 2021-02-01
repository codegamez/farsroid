// Website Scripts

$(document).ready(function () {

   let appStoreButton = $("a.appstore_button");
   appStoreButton.fadeTo("slow", 1.0);
   appStoreButton.hover(function () {
      $(this).fadeTo("slow", 0.6);
   }, function () {
      $(this).fadeTo("slow", 1.0);
   });

   let playStoreButton = $("a.google_play_button");
   playStoreButton.fadeTo("fast", 1.0);
   playStoreButton.hover(function () {
      $(this).fadeTo("fast", 0.7);
   }, function () {
      $(this).fadeTo("fast", 1.0);
   });
});


$(window).bind("load", function () {

   $(".loading").fadeOut(400);

   $(".header").fadeIn(400);

   $(".header_left, .header_right").delay(600).fadeIn(400);

   $(".app_icon").hover(
      function () {
         $(this).addClass("animated wobble");
      }, function () {
         $(this).removeClass("animated wobble");
      });


   setTimeout(function () {

      $(".footer, .share_support_section").show().addClass('animated fadeInUp');

   }, 1500);

});