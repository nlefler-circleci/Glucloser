//
// Created by Nathan Lefler on 5/2/14.
// Copyright (c) 2014 NLefler. All rights reserved.
//


@interface UIViewController (GLSlideMenuMemberController)

- (NSString *)slideMenuTitle;

@end

@interface GLSlideMenuController : UIViewController

- (instancetype)initWithMenuWidth:(CGFloat)width;

- (void)addViewControllers:(NSArray *)viewControllers;

- (void)setOpenImage:(UIImage *)openImage;

@end